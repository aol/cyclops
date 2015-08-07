package com.aol.simple.react.async;

public class DirectWaitStrategy<T> implements WaitStrategy<T> {

	@Override
	public T take(com.aol.simple.react.async.WaitStrategy.Takeable<T> t)
			throws InterruptedException {
		return t.take();
	}

	@Override
	public boolean offer(com.aol.simple.react.async.WaitStrategy.Offerable o)
			throws InterruptedException {
		return o.offer();
	}

}
