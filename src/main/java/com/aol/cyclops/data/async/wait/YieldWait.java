package com.aol.cyclops.data.async.wait;

public class YieldWait<T> implements WaitStrategy<T> {

    @Override
    public T take(final WaitStrategy.Takeable<T> t) throws InterruptedException {
        T result;

        while ((result = t.take()) == null) {
            Thread.yield();
        }

        return result;
    }

    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        while (!o.offer()) {
            Thread.yield();
        }
        return true;
    }

}
