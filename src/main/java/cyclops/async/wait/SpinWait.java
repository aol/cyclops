package cyclops.async.wait;

import java.util.concurrent.locks.LockSupport;

/**
 * Repeatedly retry toNested take or offer element toNested Queue if full or data unavailable,
 * with a wait of 1 nano second between retries
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the async.Queue
 */
public class SpinWait<T> implements WaitStrategy<T> {

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#take(cyclops2.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final WaitStrategy.Takeable<T> t) throws InterruptedException {
        T result;

        while ((result = t.take()) == null) {
            LockSupport.parkNanos(1l);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see cyclops2.async.wait.WaitStrategy#offer(cyclops2.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        while (!o.offer()) {
            LockSupport.parkNanos(1l);
        }
        return true;
    }

}
