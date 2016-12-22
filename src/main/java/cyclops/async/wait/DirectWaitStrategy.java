package cyclops.async.wait;

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
     * @see cyclops.async.wait.WaitStrategy#take(cyclops.async.wait.WaitStrategy.Takeable)
     */
    @Override
    public T take(final cyclops.async.wait.WaitStrategy.Takeable<T> t) throws InterruptedException {
        return t.take();
    }

    /* (non-Javadoc)
     * @see cyclops.async.wait.WaitStrategy#offer(cyclops.async.wait.WaitStrategy.Offerable)
     */
    @Override
    public boolean offer(final cyclops.async.wait.WaitStrategy.Offerable o) throws InterruptedException {
        return o.offer();
    }

}
