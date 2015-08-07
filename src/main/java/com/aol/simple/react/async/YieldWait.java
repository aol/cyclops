package com.aol.simple.react.async;


public class YieldWait<T> implements WaitStrategy<T> {

	@Override
	public T take(WaitStrategy.Takeable<T> t) throws InterruptedException {
		T result;

		while ((result = t.take()) == null) {
			Thread.yield();
		}

		return result;
	}

	@Override
	public boolean offer(WaitStrategy.Offerable o) throws InterruptedException {
		while (!o.offer()) {
			Thread.yield();
		}
		return true;
	}

}
