package com.oath.cyclops.async.wait;

/**
 * Repeatedly retry to take or offer element to Queue if full or data unavailable
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the async.Queue
 */
public class NoWaitRetry<T> implements WaitStrategy<T> {

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#take(cyclops2.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final com.oath.cyclops.async.wait.WaitStrategy.Takeable<T> t) throws InterruptedException {
        T result;

        while ((result = t.take()) == null) {

        }

        return result;
    }

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#offer(cyclops2.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        while (!o.offer()) {

        }
        return true;
    }

}
