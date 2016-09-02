package com.aol.cyclops.internal.stream;

import com.aol.cyclops.control.Streamable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StreamableImpl<T> implements Streamable<T> {
    @Getter
    private final Iterable<T> streamable;

    public String toString() {
        return String.format("Streamable[%s]", streamable);
    }
}