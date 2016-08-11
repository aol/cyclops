package com.aol.cyclops.data.async.wait;

import java.util.concurrent.locks.LockSupport;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExponentialBackofWaitStrategy<T> implements WaitStrategy<T> {

    private final double backoffNanos;
    private final double coefficient;

    public ExponentialBackofWaitStrategy() {
        this.backoffNanos = 1;
        this.coefficient = 1.1;
    }

    @Override
    public T take(WaitStrategy.Takeable<T> t) throws InterruptedException {
        double currentBackoff = backoffNanos;
        T result;

        while ((result = t.take()) == null) {
            LockSupport.parkNanos((long) currentBackoff);
            currentBackoff = (currentBackoff * coefficient);

        }

        return result;
    }

    @Override
    public boolean offer(WaitStrategy.Offerable o) throws InterruptedException {
        double currentBackoff = backoffNanos;
        while (!o.offer()) {
            LockSupport.parkNanos((long) currentBackoff);
            currentBackoff = (currentBackoff * coefficient);

        }
        return true;
    }

}
