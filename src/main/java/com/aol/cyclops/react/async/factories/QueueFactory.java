package com.aol.cyclops.react.async.factories;

import com.aol.cyclops.react.async.Queue;

public interface QueueFactory<T> {

	public Queue<T> build();
}
