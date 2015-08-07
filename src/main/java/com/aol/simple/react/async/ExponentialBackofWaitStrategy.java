package com.aol.simple.react.async;

import java.util.concurrent.locks.LockSupport;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public class ExponentialBackofWaitStrategy<T> implements WaitStrategy<T> {

	private final long backoffNanos; 
	private final double coefficient;
	
	public ExponentialBackofWaitStrategy(){
		this.backoffNanos=1l;
		this.coefficient=1.3;
	}

	@Override
	public T take(WaitStrategy.Takeable<T> t)
			throws InterruptedException {
		long currentBackoff = backoffNanos;
		T result;

		while ((result = t.take()) == null) {
			LockSupport.parkNanos(currentBackoff);
			currentBackoff = (long) (currentBackoff * coefficient);
		}

		return result;
	}

	@Override
	public boolean offer(WaitStrategy.Offerable o) throws InterruptedException {
		long currentBackoff = backoffNanos;
		while (!o.offer()) {
			LockSupport.parkNanos(currentBackoff);
			currentBackoff = (long) (currentBackoff * coefficient);
		}
		return true;
	}

}
