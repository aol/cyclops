package com.aol.cyclops.internal.stream;

import cyclops.stream.Streamable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StreamableImpl<T> implements Streamable<T> {
    @Getter
    private final Iterable<T> streamable;

    @Override
    public String toString() {
        return String.format("Streamable[%s]", streamable);
    }
}