package com.oath.cyclops.async.wait;

/**
 * Repeatedly retry to take or offer element to Queue if full or data unavailable, calling Thread.yield in between attempts
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the async.Queue
 */
public class YieldWait<T> implements WaitStrategy<T> {

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#take(cyclops2.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final WaitStrategy.Takeable<T> t) throws InterruptedException {
        T result;

        while ((result = t.take()) == null) {
            Thread.yield();
        }

        return result;
    }

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#offer(cyclops2.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        while (!o.offer()) {
            Thread.yield();
        }
        return true;
    }

}
