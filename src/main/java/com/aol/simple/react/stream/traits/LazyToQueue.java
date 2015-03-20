package com.aol.simple.react.stream.traits;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;

public interface LazyToQueue <U> extends ToQueue<U>{


	
	abstract QueueFactory<U> getQueueFactory();
	abstract <T, R>  SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
		
	
	abstract <R> SimpleReactStream<R> then(final Function<U, R> fn, ExecutorService exec);
	abstract ExecutorService getPopulator();

	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();
		
		
			then(queue::offer,getPopulator()).run((Thread)null,
					() -> queue.close());

		
		return queue;
	}
	
	
	default U add(U value,Queue<U> queue){
		if(!queue.add(value))
			throw new RuntimeException();
		return value;
	}
}
