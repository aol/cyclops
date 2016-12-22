package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import cyclops.Streams;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LimitWhileOperator<U> {

    private final Stream<U> stream;

    public Stream<U> limitWhile(final Predicate<? super U> predicate) {
        final Iterator<U> it = stream.iterator();
        return Streams.stream(new Iterator<U>() {
            U next;
            boolean nextSet = false;
            boolean stillGoing = true;

            @Override
            public boolean hasNext() {
                if (!stillGoing)
                    return false;
                if (nextSet)
                    return stillGoing;

                if (it.hasNext()) {
                    next = it.next();
                    nextSet = true;
                    if (!predicate.test(next)) {
                        stillGoing = false;
                    }

                } else {
                    stillGoing = false;
                }
                return stillGoing;

            }

            @Override
            public U next() {

                if (nextSet) {
                    nextSet = false;
                    return next;
                }

                final U local = it.next();
                if (stillGoing) {
                    stillGoing = !predicate.test(local);
                }
                return local;
            }

        });
    }
}
