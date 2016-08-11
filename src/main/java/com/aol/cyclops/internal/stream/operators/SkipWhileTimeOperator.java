package com.aol.cyclops.internal.stream.operators;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SkipWhileTimeOperator<U> {
    private final Stream<U> stream;

    public Stream<U> skipWhile(long time, TimeUnit unit) {
        long start = System.nanoTime();
        long allowed = unit.toNanos(time);
        return stream.filter(a -> System.nanoTime() - start > allowed);

    }
}
