package com.aol.simple.react.stream.api;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.stream.FutureStream;

public interface SyncToQueue <U> extends ToQueue<U>{

	
	abstract QueueFactory<U> getQueueFactory();
	abstract List<U> block();
	

	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = QueueFactories.<U>unBoundedQueue().build();

		block().forEach(it-> queue.add(it));
		
		return queue;
	}
}
