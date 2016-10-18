package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LimitWhileTimeOperator<U> {

    private final Stream<U> stream;

    public Stream<U> limitWhile(final long time, final TimeUnit unit) {
        final Iterator<U> it = stream.iterator();
        final long start = System.nanoTime();
        final long allowed = unit.toNanos(time);
        return StreamUtils.stream(new Iterator<U>() {
            U next;
            boolean stillGoing = true;

            @Override
            public boolean hasNext() {
                stillGoing = System.nanoTime() - start < allowed;
                if (!stillGoing)
                    return false;
                return it.hasNext();

            }

            @Override
            public U next() {
                if (!stillGoing)
                    throw new NoSuchElementException();

                final U val = it.next();
                stillGoing = System.nanoTime() - start < allowed;
                return val;

            }

        });
    }
}
