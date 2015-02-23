package com.aol.simple.react.stream.traits;

import com.aol.simple.react.async.Queue;

public interface EagerOrLazyToQueue<U> extends EagerToQueue<U>, LazyToQueue<U>{
	
	public boolean isEager();
	
	default Queue<U> toQueue() {
		if(isEager())
			return EagerToQueue.super.toQueue();
		else
			return LazyToQueue.super.toQueue();
	}

}
