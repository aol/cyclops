package com.aol.cyclops.react.async.wait;

import java.util.concurrent.locks.LockSupport;


/**
 * Repeatedly retry to take or offer element to Queue if full or data unavailable,
 * with a wait of 1 nano second between retries
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
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
