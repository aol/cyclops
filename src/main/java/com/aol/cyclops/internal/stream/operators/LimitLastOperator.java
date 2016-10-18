package com.aol.cyclops.internal.stream.operators;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;

public class LimitLastOperator<T> {

    private final Stream<T> stream;
    private final ArrayDeque<T> buffer;
    private final int limit;

    public LimitLastOperator(final Stream<T> stream, final int limit) {
        buffer = new ArrayDeque<>(
                                  limit);
        this.stream = stream;
        this.limit = limit;
    }

    public Stream<T> limitLast() {
        final Iterator<T> it = stream.iterator();
        return StreamUtils.stream(new Iterator<T>() {

            @Override
            public boolean hasNext() {
                while (it.hasNext()) {
                    buffer.add(it.next());
                    if (buffer.size() > limit)
                        buffer.pop();
                }
                return buffer.size() > 0;
            }

            @Override
            public T next() {
                return buffer.pop();
            }

        });
    }
}
