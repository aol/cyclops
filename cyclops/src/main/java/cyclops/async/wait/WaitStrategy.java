package cyclops.async.wait;

/**
 * An interface that defines a Waiting strategy to be employed when an async.Queue is full or zero
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in Queue
 */
public interface WaitStrategy<T> {

    /**
     * Represents something that may provide data
     * 
     * @author johnmcclean
     *
     * @param <T> Data type of elements in Takeable
     */
    public static interface Takeable<T> {
        /** 
         * Take data from data provider
         * 
         * @return Data taken
         * @throws InterruptedException
         */
        public T take() throws InterruptedException;
    }

    /**
     * Represents something that may recieve data
     * 
     * @author johnmcclean
     *
     */
    public static interface Offerable {
        
        /**
         * Offer data to recievable type
         *  
         * @return true if data accepted
         * @throws InterruptedException
         */
        public boolean offer() throws InterruptedException;
    }

    /**
     * Attempt to take from Takeable
     * @param t  Takeable to take next data point from
     * @return Data taken
     * @throws InterruptedException
     */
    public T take(Takeable<T> t) throws InterruptedException;

    /**
     * Push data into a recievable type using the provided offerable
     * 
     * @param o Offerable to push data to recievable type
     * @return true if data pushed via Offerable
     * @throws InterruptedException
     */
    public boolean offer(Offerable o) throws InterruptedException;

    /**
     * @return YieldWait strategy {@see YieldWait}
     */
    static <T> YieldWait<T> yieldWait() {
        return new YieldWait<>();
    }

    /**
     * @return SpinWait strategy {@see SpinWait}
     */
    static <T> SpinWait<T> spinWait() {
        return new SpinWait<>();
    }

    /**
     * @return NoWaitRetry strategy {@see NoWaitRetry}
     */
    static <T> NoWaitRetry<T> noWaitRetry() {
        return new NoWaitRetry<>();
    }

    /**
     * @return ExponentialBackofWaitStrategy {@see ExponentialBackofWaitStrategy}
     */
    static <T> ExponentialBackofWaitStrategy<T> exponentialBackOff() {
        return new ExponentialBackofWaitStrategy<>();
    }

    /**
     * @return DirectWaitStrategy {@see DirectWaitStrategy}
     */
    static <T> DirectWaitStrategy<T> direct() {
        return new DirectWaitStrategy<>();
    }
}
