package com.aol.simple.react.stream.api;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.stream.FutureStream;

public interface AsyncToQueue<U> extends ToQueue<U>{

	abstract boolean isEager();
	abstract QueueFactory<U> getQueueFactory();
	abstract <T, R> FutureStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
		
	abstract <R> FutureStream<R> then(final Function<U, R> fn);	
	

	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();

		if (isEager())
			then(it -> queue.offer(it)).allOf(it -> queue.close());
		else {

			then(it -> queue.offer(it)).run(new ForkJoinPool(1),
					() -> queue.close());

		}
		return queue;
	}
}
