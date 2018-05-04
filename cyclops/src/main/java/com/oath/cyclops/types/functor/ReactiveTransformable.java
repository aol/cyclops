package com.oath.cyclops.types.functor;

import com.oath.cyclops.util.ExceptionSoftener;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public interface ReactiveTransformable<T> extends Transformable<T> {
    /**
     * Retry a transformation if it fails. Default settings are to retry up to 7
     * times, with an doubling backoff period starting @ 2 seconds delay before
     * retry.
     *
     *
     * @param fn
     *            Function to retry if fails
     *
     */
    default <R> Transformable<R> retry(final Function<? super T, ? extends R> fn) {
        return retry(fn, 7, 2, TimeUnit.SECONDS);
    }

    /**
     * Retry a transformation if it fails. Retries up to <b>retries</b>
     * times, with an doubling backoff period starting @ <b>delay</b> TimeUnits delay before
     * retry.
     *
     *
     * @param fn
     *            Function to retry if fails
     * @param retries
     *            Number of retries
     * @param delay
     *            Delay in TimeUnits
     * @param timeUnit
     *            TimeUnit to use for delay
     */
    default <R> Transformable<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        final Function<T, R> retry = t -> {
            final long[] sleep = { timeUnit.toMillis(delay) };
            Throwable exception = null;
            for (int count = retries; count >=0; count--) {
                try {
                    return fn.apply(t);
                } catch (final Throwable e) {
                    exception = e;
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep[0]))
                        .run();
                    sleep[0] = sleep[0] * 2;
                }
            }
            throw ExceptionSoftener.throwSoftenedException(exception);

        };
        return map(retry);
    }

}
