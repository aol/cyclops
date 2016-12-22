package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import cyclops.Streams;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DebounceOperator<T> {

    private final Stream<T> stream;

    public Stream<T> debounce(final long time, final TimeUnit t) {
        final Iterator<T> it = stream.iterator();
        final long timeNanos = t.toNanos(time);
        return Streams.stream(new Iterator<T>() {
            volatile long last = 0;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                long elapsedNanos = 1;
                T nextValue = null;
                while (elapsedNanos > 0 && it.hasNext()) {

                    nextValue = it.next();
                    if (last == 0) {
                        last = System.nanoTime();
                        return nextValue;
                    }
                    elapsedNanos = timeNanos - (System.nanoTime() - last);
                }

                last = System.nanoTime();
                if (it.hasNext())
                    return nextValue;
                else if (elapsedNanos <= 0)
                    return nextValue;
                else
                    return (T) DEBOUNCED;
            }

        })
                          .filter(i -> i != DEBOUNCED);
    }

    private final static Object DEBOUNCED = new Object();
}
