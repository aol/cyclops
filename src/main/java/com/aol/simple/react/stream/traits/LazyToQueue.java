package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.lazy.LazyReact;

public interface LazyToQueue<U> extends ToQueue<U> {

	abstract <T, R> LazyFutureStream<R> allOf(final Collector collector,
			final Function<T, R> fn);

	abstract <R> LazyFutureStream<R> then(final Function<U, R> fn,
			Executor exec);

	abstract <R> LazyFutureStream<R> thenSync(final Function<U, R> fn);
	abstract <T extends BaseSimpleReact> T getPopulator();

	/**
	 * Convert the current Stream to a simple-react Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();

		
		
		Continuation continuation = thenSync(queue::add).runContinuation(() -> {
			queue.close(); });
		queue.setContinuation(continuation);
		return queue;
	}

	/* 
	 * Convert the current Stream to a simple-react Queue.
	 * The supplied function can be used to determine properties of the Queue to be used
	 * 
	 *  @param fn Function to be applied to default Queue. Returned Queue will be used to conver this Stream to a Queue
	 *	@return This stream converted to a Queue
	 * @see com.aol.simple.react.stream.traits.ToQueue#toQueue(java.util.function.Function)
	 */
	default Queue<U> toQueue(Function<Queue, Queue> fn) {
		Queue<U> queue = fn.apply(this.getQueueFactory().build());

		Continuation continuation = thenSync(queue::add).runContinuation(() -> {
			queue.close();
			
		});
		queue.setContinuation(continuation);
		return queue;
	}

	/* 
	 * Populate provided queues with the sharded data from this Stream.
	 * 
	 *	@param shards Map of key to Queue shards
	 *	@param sharder Sharding function, element to key converter
	 * @see com.aol.simple.react.stream.traits.ToQueue#toQueue(java.util.Map, java.util.function.Function)
	 */
	default <K> void toQueue(Map<K, Queue<U>> shards, Function<U, K> sharder) {

		//in this case all the items have to be pushed to the shards, 
		//we can't rely on the client pulling them all to get them in to the right shards
		LazyReact service = getPopulator();
		then(it -> shards.get(sharder.apply(it)).offer(it),
				service.getExecutor())
				.runThread(() -> {
					shards.values().forEach(it -> it.close());
					returnPopulator(service);
				});

	}

	abstract <T extends BaseSimpleReact> void returnPopulator(T service);

	default U add(U value, Queue<U> queue) {
		if (!queue.add(value))
			throw new RuntimeException();
		return value;
	}
}
