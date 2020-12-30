package com.oath.cyclops.internal.stream.operators;

import cyclops.companion.Streams;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

@AllArgsConstructor
public class OnePerOperator<T> {

    private final Stream<T> stream;

    public Stream<T> onePer(final long time, final TimeUnit t) {
        final Iterator<T> it = stream.iterator();
        final long next = t.toNanos(time);
        return Streams.stream(new Iterator<T>() {
            volatile long last = -1;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {

                final T nextValue = it.next();

                final long sleepFor = next - (System.nanoTime() - last);

                LockSupport.parkNanos(sleepFor);

                last = System.nanoTime();
                return nextValue;
            }

        });
    }
}
