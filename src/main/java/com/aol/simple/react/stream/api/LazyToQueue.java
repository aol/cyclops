package com.aol.simple.react.stream.api;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.async.QueueFactory;

public interface LazyToQueue <U> extends ToQueue<U>{


	
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


			then(it -> queue.offer(it)).run(new ForkJoinPool(1),
					() -> queue.close());

		
		return queue;
	}
}
