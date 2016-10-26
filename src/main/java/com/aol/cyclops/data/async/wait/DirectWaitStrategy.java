package com.aol.cyclops.data.async.wait;

/**
 * Will try to access the queue once, and return the result directly from the Queue
 * 
 * Effectively the same as calling queue.take() / queue.offer(T val)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in async.Queue
 */
public class DirectWaitStrategy<T> implements WaitStrategy<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.async.wait.WaitStrategy#take(com.aol.cyclops.data.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final com.aol.cyclops.data.async.wait.WaitStrategy.Takeable<T> t) throws InterruptedException {
        return t.take();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.async.wait.WaitStrategy#offer(com.aol.cyclops.data.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final com.aol.cyclops.data.async.wait.WaitStrategy.Offerable o) throws InterruptedException {
        return o.offer();
    }

}
