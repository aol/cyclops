package com.aol.cyclops.data.async.wait;


/**
 * Will try to access the queue once, and return the result directly from the Queue
 * 
 * Effectively the same as calling queue.take() / queue.offer(T val)
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class DirectWaitStrategy<T> implements WaitStrategy<T> {

	@Override
	public T take(com.aol.cyclops.data.async.wait.WaitStrategy.Takeable<T> t)
			throws InterruptedException {
		return t.take();
	}

	@Override
	public boolean offer(com.aol.cyclops.data.async.wait.WaitStrategy.Offerable o)
			throws InterruptedException {
		return o.offer();
	}

}
