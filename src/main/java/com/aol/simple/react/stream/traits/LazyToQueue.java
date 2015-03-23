package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.lazy.LazyReact;

public interface LazyToQueue <U> extends ToQueue<U>{


	
	
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
			then(queue::offer,service.getExecutor()).runThread(
					() -> {queue.close(); returnPopulator(service); });

		
		return queue;
	}
	
	default Queue<U> toQueue(Function<Queue,Queue> fn) {
		Queue<U> queue = fn.apply(this.getQueueFactory().build());
		
		 	LazyReact service = getPopulator();
			then(queue::offer,service.getExecutor()).runThread(
					() -> {queue.close(); returnPopulator(service); });

		
		return queue;
	}
	default<K> void toQueue(Map<K,Queue<U>> shards, Function<U,K> sharder) {
		
		
		 	LazyReact service = getPopulator();
			then(it-> shards.get(sharder.apply(it)).offer(it),service.getExecutor())
							.capture(Throwable::printStackTrace)
							.runThread(
					() -> {shards.values().forEach(it->it.close()); returnPopulator(service); });

		
		
	}
	
	
	abstract<T extends BaseSimpleReact> void returnPopulator(T service);
	default U add(U value,Queue<U> queue){
		if(!queue.add(value))
			throw new RuntimeException();
		return value;
	}
}
