package com.aol.cyclops.internal.stream.operators;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;

public class SkipLastOperator<T> {

    private final Stream<T> stream;
    private final ArrayDeque<T> buffer;
    private final int skip;

    public SkipLastOperator(final Stream<T> stream, final int skip) {
        buffer = new ArrayDeque<>(
                                  skip);
        this.stream = stream;
        this.skip = skip;
    }

    public Stream<T> skipLast() {
        final Iterator<T> it = stream.iterator();
        return StreamUtils.stream(new Iterator<T>() {
            boolean finished = false;

            @Override
            public boolean hasNext() {
                while (buffer.size() < skip && it.hasNext()) {
                    buffer.add(it.next());
                }
                return finished = it.hasNext();
            }

            @Override
            public T next() {
                if (finished && buffer.size() == 0)
                    throw new NoSuchElementException();
                return buffer.pop();
            }

        });
    }
}
