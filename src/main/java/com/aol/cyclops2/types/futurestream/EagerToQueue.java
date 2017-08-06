package com.aol.cyclops2.types.futurestream;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

import cyclops.async.adapters.Queue;
import cyclops.async.adapters.QueueFactory;

public interface EagerToQueue<U> extends ToQueue<U> {

    @Override
    abstract QueueFactory<U> getQueueFactory();

    abstract <R1, R2> SimpleReactStream<R2> allOf(final Collector<? super U, ?, R1> collector, final Function<? super R1, ? extends R2> fn);

    abstract <R> SimpleReactStream<R> thenSync(final Function<? super U, ? extends R> fn);

    /**
     * Convert the current Stream to a SimpleReact Queue
     * 
     * @return Queue populated asynchrnously by this Stream
     */
    @Override
    default Queue<U> toQueue() {
        final Queue<U> queue = this.getQueueFactory()
                                   .build();

        thenSync(it -> queue.offer(it)).allOf(it -> queue.close());

        return queue;
    }

    /* 
     * Convert the current Stream to a simple-react Queue.
     * The supplied function can be used to determine properties of the Queue to be used
     * 
     *  @param fn Function to be applied to default Queue. Returned Queue will be used to conver this Stream to a Queue
     *	@return This reactiveStream converted to a Queue
     * @see com.aol.cyclops2.react.reactiveStream.traits.ToQueue#toQueue(java.util.function.Function)
     */
    @Override
    default Queue<U> toQueue(final Function<Queue, Queue> modifier) {
        final Queue<U> queue = modifier.apply(this.getQueueFactory()
                                                  .build());
        thenSync(it -> queue.offer(it)).allOf(it -> queue.close());

        return queue;
    }

    @Override
    default void addToQueue(final Queue queue) {
        thenSync(it -> queue.offer(it)).allOf(it -> queue.close());
    }

    /* 
     * Populate provided queues with the sharded data from this Stream.
     * 
     *	@param shards Map of key to Queue shards
     *	@param sharder Sharding function, element to key converter
     * @see com.aol.cyclops2.react.reactiveStream.traits.ToQueue#toQueue(java.util.Map, java.util.function.Function)
     */
    @Override
    default <K> void toQueue(final Map<K, Queue<U>> shards, final Function<? super U, ? extends K> sharder) {

        thenSync(it -> shards.get(sharder.apply(it))
                             .offer(it)).allOf(data -> {
                                 shards.values()
                                       .forEach(it -> it.close());
                                 return true;
                             });

    }
}
