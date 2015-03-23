package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.function.Function;

import com.aol.simple.react.async.Queue;

public interface EagerOrLazyToQueue<U> extends EagerToQueue<U>, LazyToQueue<U>{
	
	public boolean isEager();
	
	default Queue<U> toQueue() {
		if(isEager())
			return EagerToQueue.super.toQueue();
		else
			return LazyToQueue.super.toQueue();
	}
	default Queue<U> toQueue(Function<Queue,Queue> fn) {
		if(isEager())
			return EagerToQueue.super.toQueue(fn);
		else
			return LazyToQueue.super.toQueue(fn);
	}
	default <K> void toQueue(Map<K, Queue<U>> shards, Function<U, K> sharder) {
		if(isEager())
			EagerToQueue.super.toQueue(shards,sharder);
		else
			LazyToQueue.super.toQueue(shards,sharder);
	}

}
