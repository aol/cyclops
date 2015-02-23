package com.aol.simple.react.stream.traits;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;

public interface EagerToQueue<U> extends ToQueue<U>{

	
	abstract QueueFactory<U> getQueueFactory();
	abstract <T, R>  SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
		
	abstract <R> SimpleReactStream<R> then(final Function<U, R> fn);	
	

	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();

		
		then(it -> queue.offer(it)).allOf(it -> queue.close());
		
		return queue;
	}
}
