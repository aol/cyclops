package com.aol.simple.react.stream.traits;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.lazy.LazyReact;

public interface LazyToQueue <U> extends ToQueue<U>{


	
	abstract QueueFactory<U> getQueueFactory();
	abstract <T, R>  SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
		
	
	abstract <R> SimpleReactStream<R> then(final Function<U, R> fn, ExecutorService exec);
	abstract<T extends BaseSimpleReact> T getPopulator();

	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();
		
		 	LazyReact service = getPopulator();
			then(queue::offer,service.getExecutor()).run((Thread)null,
					() -> {queue.close(); returnPopulator(service); });

		
		return queue;
	}
	
	
	abstract<T extends BaseSimpleReact> void returnPopulator(T service);
	default U add(U value,Queue<U> queue){
		if(!queue.add(value))
			throw new RuntimeException();
		return value;
	}
}
