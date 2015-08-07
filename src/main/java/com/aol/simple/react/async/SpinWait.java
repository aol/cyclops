package com.aol.simple.react.async;

import java.util.concurrent.locks.LockSupport;


public class SpinWait<T> implements WaitStrategy<T> {

	@Override
	public T take(WaitStrategy.Takeable<T> t) throws InterruptedException {
		T result;

		while ((result = t.take()) == null) {
			LockSupport.parkNanos(1l);
		}

		return result;
	}

	@Override
	public boolean offer(WaitStrategy.Offerable o) throws InterruptedException {
		while (!o.offer()) {
			LockSupport.parkNanos(1l);
		}
		return true;
	}

}
