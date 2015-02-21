package com.aol.simple.react.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;


public class QueueFactories {

	public static<T> QueueFactory<T> boundedQueue(int queueSize){
		return () -> new Queue<T>(new LinkedBlockingQueue<>(queueSize));
	}
	public static<T> QueueFactory<T> unboundedQueue(){
		return () -> new Queue<T>();
	}
	
	public static<T> QueueFactory<T> synchronousQueue(){
		return () -> new Queue<T>(new SynchronousQueue<>());
	}
}
