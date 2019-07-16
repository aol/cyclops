package com.oath.cyclops.types.futurestream;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import cyclops.futurestream.LazyReact;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.adapters.Queue.ClosedQueueException;
import com.oath.cyclops.internal.react.async.future.CompletedException;
import cyclops.futurestream.FutureStream;

public interface LazyToQueue<U> extends ToQueue<U> {

    <R> FutureStream<R> then(final Function<? super U, ? extends R> fn, Executor exec);

    <R> FutureStream<R> thenSync(final Function<? super U, ? extends R> fn);

    LazyReact getPopulator();

    FutureStream<U> peekSync(final Consumer<? super U> consumer);

    /**
     * Convert the current Stream to a simple-react Queue
     *
     * @return Queue populated asynchrnously by this Stream
     */
    @Override
    default Queue<U> toQueue() {
        final Queue<U> queue = getQueueFactory().build();

        final Continuation continuation = peekSync(queue::add).capture(e->{
            queue.addError(e);
        }).self(s -> {
            if (this.getPopulator()
                    .isPoolingActive())
                s.peekSync(v -> {
                    throw new CompletedException(
                            v);
                });
        })
                .runContinuation(() -> {
                    queue.close();
                });

        queue.addContinuation(continuation);
        return queue;
    }

    /*
     * Convert the current Stream to a simple-react Queue.
     * The supplied function can be used to determine properties of the Queue to be used
     *
     *  @param fn Function to be applied to default Queue. Returned Queue will be used to conver this Stream to a Queue
     *	@return This stream converted to a Queue
     * @see com.oath.cyclops.react.stream.traits.ToQueue#toQueue(java.util.function.Function)
     */
    @Override
    default Queue<U> toQueue(final Function<Queue, Queue> fn) {
        final Queue<U> queue = fn.apply(getQueueFactory().build());

        final Continuation continuation = thenSync(queue::add).self(s -> {
            if (this.getPopulator().isPoolingActive())
                s.peekSync(v -> {
                    throw new CompletedException(
                                                 v);
                });
        }).runContinuation(() -> {queue.close();});
        queue.addContinuation(continuation);
        return queue;
    }

    @Override
    default void addToQueue(final Queue queue) {
        FutureStream str = thenSync(queue::add).self(s -> {
            if (this.getPopulator()
                .isPoolingActive())
                s.peekSync(v -> {
                    throw new CompletedException(
                        v);
                });
        });


        final Continuation continuation =  queue.getContinuationStrategy().isBlocking() ? str.blockingContinuation(() -> {
            throw new ClosedQueueException();
        }) : str.runContinuation(() -> {throw new ClosedQueueException();
                                   }
                           );
        queue.addContinuation(continuation);

    }

    /*
     * Populate provided queues with the sharded data from this Stream.
     *
     *	@param shards Map of key to Queue shards
     *	@param sharder Sharding function, element to key converter
     * @see com.oath.cyclops.react.stream.traits.ToQueue#toQueue(java.util.Map, java.util.function.Function)
     */
    @Override
    default <K> void toQueue(final Map<K, Queue<U>> shards, final Function<? super U, ? extends K> sharder) {

        //in this case all the items have to be pushed to the shards,
        //we can't rely on the client pulling them all to getValue them in to the right shards
        final LazyReact service = getPopulator();
        then(it -> shards.get(sharder.apply(it))
                         .offer(it),
             service.getExecutor()).runThread(() -> {
                 shards.values()
                       .forEach(it -> it.close());
                 returnPopulator(service);
             });

    }

    void returnPopulator(LazyReact service);

    default U add(final U value, final Queue<U> queue) {
        if (!queue.add(value))
            throw new RuntimeException();
        return value;
    }
}
