package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.function.Function;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;

public interface ToQueue <U>{
	abstract  Queue<U> toQueue();
	abstract<K> void toQueue(Map<K,Queue<U>> shards, Function<U,K> sharder);
	abstract QueueFactory<U> getQueueFactory();
	
	
}
