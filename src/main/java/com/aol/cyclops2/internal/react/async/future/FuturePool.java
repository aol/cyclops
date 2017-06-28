package com.aol.cyclops2.internal.react.async.future;

import java.util.function.Supplier;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import lombok.AllArgsConstructor;

/**
 * Active consumer / multiple producer future pool
 * 
 * @author johnmcclean
 *
 */
@AllArgsConstructor
public class FuturePool {

    private final ManyToOneConcurrentArrayQueue<FastFuture> pool;
    private final int max;

    public <T> FastFuture<T> next(final Supplier<FastFuture<T>> factory) {
        if (pool.size() > 0) {

            final FastFuture next = pool.poll();
            next.clearFast();
            return next;
        }

        return factory.get();
    }

    public <T> void done(final FastFuture<T> f) {
        if (pool.size() < max) {

            pool.offer(f);
        }

    }
}
