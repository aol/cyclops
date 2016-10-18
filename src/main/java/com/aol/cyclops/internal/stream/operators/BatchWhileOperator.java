package com.aol.cyclops.internal.stream.operators;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.data.collections.extensions.standard.ListXImpl;

public class BatchWhileOperator<T, C extends Collection<? super T>> {
    private static final Object UNSET = new Object();
    private final Stream<T> stream;
    private final Supplier<C> factory;

    public BatchWhileOperator(final Stream<T> stream) {
        this.stream = stream;
        factory = () -> (C) new ListXImpl();
    }

    public BatchWhileOperator(final Stream<T> stream, final Supplier<C> factory) {
        super();
        this.stream = stream;
        this.factory = factory;
    }

    public Stream<C> batchWhile(final Predicate<? super T> predicate) {
        final Iterator<T> it = stream.iterator();
        return StreamUtils.stream(new Iterator<C>() {
            T value = (T) UNSET;

            @Override
            public boolean hasNext() {
                return value != UNSET || it.hasNext();
            }

            @Override
            public C next() {

                final C list = factory.get();
                if (value != UNSET)
                    list.add(value);
                T value;

                label: while (it.hasNext()) {
                    value = it.next();
                    list.add(value);

                    if (!predicate.test(value)) {
                        value = (T) UNSET;
                        break label;
                    }
                    value = (T) UNSET;

                }
                return list;
            }

        })
                          .filter(l -> l.size() > 0);
    }

}
