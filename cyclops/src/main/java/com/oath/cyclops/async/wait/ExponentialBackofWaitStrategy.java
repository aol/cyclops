package com.oath.cyclops.async.wait;

import lombok.AllArgsConstructor;

import java.util.concurrent.locks.LockSupport;

/**
 * Will recover to recieve or push data to a Queue, backing off by an exponentially increasing wait time
 * until successful.
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the async.Queue
 */
@AllArgsConstructor
public class ExponentialBackofWaitStrategy<T> implements WaitStrategy<T> {

    private final double backoffNanos;
    private final double coefficient;

    public ExponentialBackofWaitStrategy() {
        this.backoffNanos = 1;
        this.coefficient = 1.1;
    }

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#take(cyclops2.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final WaitStrategy.Takeable<T> t) throws InterruptedException {
        double currentBackoff = backoffNanos;
        T result;

        while ((result = t.take()) == null) {
            LockSupport.parkNanos((long) currentBackoff);
            currentBackoff = currentBackoff * coefficient;

        }

        return result;
    }

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#offer(cyclops2.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        double currentBackoff = backoffNanos;
        while (!o.offer()) {
            LockSupport.parkNanos((long) currentBackoff);
            currentBackoff = currentBackoff * coefficient;

        }
        return true;
    }

}
