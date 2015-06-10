package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;

/**
 * 
 * interface that defines the conversion of an object to a queue
 * 
 * @author johnmcclean
 *
 * @param <U> Data type
 */
public interface ToQueue <U>{
	/**
	 * @return Data in a queue
	 */
	abstract  Queue<U> toQueue();
	/**
	 * Sharded data in queues
	 * 
	 * @param shards Map of Queues sharded by key K
	 * @param sharder Sharder function
	 */
	abstract<K> void toQueue(Map<K,Queue<U>> shards, Function<U,K> sharder);
	/**
	 * @return Factory for creating Queues to be populated
	 */
	abstract QueueFactory<U> getQueueFactory();
	/**
	 * Method to create a Queue that can be modified by supplied funciton
	 * 
	 * @param modifier Function to modify default Queue
	 * @return Populated Queue.
	 */
	abstract  Queue<U> toQueue(Function<Queue,Queue> modifier);
	
}
