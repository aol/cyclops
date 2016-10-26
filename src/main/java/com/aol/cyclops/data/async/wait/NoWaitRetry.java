package com.aol.cyclops.data.async.wait;

/**
 * Repeatedly retry to take or offer element to Queue if full or data unavailable
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the async.Queue
 */
public class NoWaitRetry<T> implements WaitStrategy<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.async.wait.WaitStrategy#take(com.aol.cyclops.data.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final com.aol.cyclops.data.async.wait.WaitStrategy.Takeable<T> t) throws InterruptedException {
        T result;

        while ((result = t.take()) == null) {

        }

        return result;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.async.wait.WaitStrategy#offer(com.aol.cyclops.data.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        while (!o.offer()) {

        }
        return true;
    }

}
