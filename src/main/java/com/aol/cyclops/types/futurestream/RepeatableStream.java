package com.aol.cyclops.types.futurestream;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RepeatableStream<T> {
    private final Collection<T> col;

    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(col.iterator(), Spliterator.ORDERED), false);
    }
}
