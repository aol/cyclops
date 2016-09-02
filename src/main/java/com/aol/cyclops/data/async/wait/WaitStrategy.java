package com.aol.cyclops.data.async.wait;

public interface WaitStrategy<T> {

    public static interface Takeable<T> {
        public T take() throws InterruptedException;
    }

    public static interface Offerable {
        public boolean offer() throws InterruptedException;
    }

    public T take(Takeable<T> t) throws InterruptedException;

    public boolean offer(Offerable o) throws InterruptedException;

    static <T> YieldWait<T> yieldWait() {
        return new YieldWait<>();
    }

    static <T> SpinWait<T> spinWait() {
        return new SpinWait<>();
    }

    static <T> NoWaitRetry<T> noWaitRetry() {
        return new NoWaitRetry<>();
    }

    static <T> ExponentialBackofWaitStrategy<T> exponentialBackOff() {
        return new ExponentialBackofWaitStrategy<>();
    }

    static <T> DirectWaitStrategy<T> direct() {
        return new DirectWaitStrategy<>();
    }
}
