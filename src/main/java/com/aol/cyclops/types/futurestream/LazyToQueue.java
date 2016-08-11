package com.aol.cyclops.types.futurestream;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.internal.react.async.future.CompletedException;

public interface LazyToQueue<U> extends ToQueue<U> {

    <R> LazyFutureStream<R> then(final Function<? super U, ? extends R> fn, Executor exec);

    <R> LazyFutureStream<R> thenSync(final Function<? super U, ? extends R> fn);

    LazyReact getPopulator();

    LazyFutureStream<U> peekSync(final Consumer<? super U> consumer);

    /**
     * Convert the current Stream to a simple-react Queue
     * 
     * @return Queue populated asynchrnously by this Stream
     */
    default Queue<U> toQueue() {
        Queue<U> queue = this.getQueueFactory()
                             .build();

        Continuation continuation = peekSync(queue::add).self(s -> {
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
     * @see com.aol.cyclops.react.stream.traits.ToQueue#toQueue(java.util.function.Function)
     */
    default Queue<U> toQueue(Function<Queue, Queue> fn) {
        Queue<U> queue = fn.apply(this.getQueueFactory()
                                      .build());

        Continuation continuation = thenSync(queue::add).self(s -> {
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

    default void addToQueue(Queue queue) {

        Continuation continuation = thenSync(queue::add).self(s -> {
            if (this.getPopulator()
                    .isPoolingActive())
                s.peekSync(v -> {
                    throw new CompletedException(
                                                 v);
                });
        })
                                                        .runContinuation(() -> {
                                                            throw new ClosedQueueException();
                                                        });
        queue.addContinuation(continuation);

    }

    /* 
     * Populate provided queues with the sharded data from this Stream.
     * 
     *	@param shards Map of key to Queue shards
     *	@param sharder Sharding function, element to key converter
     * @see com.aol.cyclops.react.stream.traits.ToQueue#toQueue(java.util.Map, java.util.function.Function)
     */
    default <K> void toQueue(Map<K, Queue<U>> shards, Function<? super U, ? extends K> sharder) {

        //in this case all the items have to be pushed to the shards, 
        //we can't rely on the client pulling them all to get them in to the right shards
        LazyReact service = getPopulator();
        then(it -> shards.get(sharder.apply(it))
                         .offer(it),
             service.getExecutor()).runThread(() -> {
                 shards.values()
                       .forEach(it -> it.close());
                 returnPopulator(service);
             });

    }

    void returnPopulator(LazyReact service);

    default U add(U value, Queue<U> queue) {
        if (!queue.add(value))
            throw new RuntimeException();
        return value;
    }
}
