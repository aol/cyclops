package com.oath.cyclops.internal.stream.operators;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.companion.Streams;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

@AllArgsConstructor
public class RecoverOperator<T> {

    private final Stream<T> stream;
    private final Class<Throwable> type;

    private static final Object UNSET = new Object();

    public Stream<T> recover(final Function<Throwable, ? extends T> fn) {
        final Iterator<T> it = stream.iterator();

        return Streams.stream(new Iterator<T>() {
            T result = (T) UNSET;

            @Override
            public boolean hasNext() {
                try {
                    return it.hasNext();
                } catch (final Throwable t) {
                    if (type.isAssignableFrom(t.getClass())) {
                        result = fn.apply(t);
                        return true;
                    }
                    ExceptionSoftener.throwSoftenedException(t);
                    return false;
                }

            }

            @Override
            public T next() {
                if (result != UNSET) {
                    final T toReturn = result;
                    result = (T) UNSET;
                    return toReturn;
                }
                return it.next();

            }

        });
    }
}
