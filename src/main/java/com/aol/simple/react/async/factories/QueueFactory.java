package com.aol.simple.react.async.factories;

import com.aol.simple.react.async.Queue;

public interface QueueFactory<T> {

	public Queue<T> build();
}
