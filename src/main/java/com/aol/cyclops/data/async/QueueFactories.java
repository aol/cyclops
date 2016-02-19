package com.aol.cyclops.data.async;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import com.aol.cyclops.data.async.wait.NoWaitRetry;

import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;


public class QueueFactories {

	public static<T> QueueFactory<T> boundedQueue(int queueSize){
		return () -> new Queue<T>(new LinkedBlockingQueue<>(queueSize));
	}
	public static<T> QueueFactory<T> unboundedQueue(){
		return () -> new Queue<T>();
	}
	/**
	 * Creates an async.Queue backed by a JDK Wait Free unbounded ConcurrentLinkedQueue
	 * Wait strategy used is NoWaitRetry by default for both Consumers and Producers 
	 * (both Consumers and Producers will repeatedly retry until successful). Use 
	 *  withConsumerWaitStrategy &amp; withProducerWaitStrategy methods on the returned queue to change the 
	 *  wait strategy
	 * <pre>
	 * {@code
	 *    queue.withConsumerWaitStrategy(new DirectWaitStrategy())
	 *         .withProducerWaitStrategy(new YieldWait());
	 * }</pre>
	 * 
	 * 
	 * @return unbounded wait free queue
	 */
	public static<T> QueueFactory<T> unboundedNonBlockingQueue(){
		return () -> new Queue<T>(new ConcurrentLinkedQueue(), new NoWaitRetry(),new NoWaitRetry());
		
	}
	/**
	 * Creates an async.Queue backed by an Agrona ManyToOneConcurrentArrayQueue bounded by specified queueSize
	 *  Wait strategy used is NoWaitRetry by default for both Consumers and Producers 
	 *  (both Consumers and Producers will repeatedly retry until successful). Use 
	 *  withConsumerWaitStrategy  &amp; withProducerWaitStrategy methods on the returned queue to change the 
	 *  wait strategy
	 * <pre>
	 * {@code
	 *    queue.withConsumerWaitStrategy(new DirectWaitStrategy())
	 *         .withProducerWaitStrategy(new YieldWait());
	 * }</pre>
	 * 
	 * @param queueSize upper bound for Queue
	 * @return bounded wait free Queue
	 */
	public static<T> QueueFactory<T> boundedNonBlockingQueue(int queueSize){
		return () -> new Queue<T>(new ManyToOneConcurrentArrayQueue(queueSize),new NoWaitRetry(),new NoWaitRetry());
		
	}
	/**
	 * Creates an async.Queue backed by an Agrona OneToOneConcurrentArrayQueue bounded by specified queueSize
	 *  Wait strategy used is NoWaitRetry by default for both Consumers and Producers 
	 *  (both Consumers and Producers will repeatedly retry until successful). Use 
	 *  withConsumerWaitStrategy  &amp; withProducerWaitStrategy methods on the returned queue to change the 
	 *  wait strategy
	 * <pre>
	 * {@code
	 *    queue.withConsumerWaitStrategy(new DirectWaitStrategy())
	 *         .withProducerWaitStrategy(new YieldWait());
	 * }</pre>
	 * 
	 * @param queueSize
	 * @return
	 */
	public static<T> QueueFactory<T> singleWriterboundedNonBlockingQueue(int queueSize){
		return () -> new Queue<T>(new OneToOneConcurrentArrayQueue(queueSize),new NoWaitRetry(),new NoWaitRetry());
		
	}
	
	/**
	 * @return async.Queue backed by a Synchronous Queue
	 */
	public static<T> QueueFactory<T> synchronousQueue(){
		return () -> new Queue<T>(new SynchronousQueue<>());
	}
	
	
	
}
