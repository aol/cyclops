package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.types.extensability.Comprehender;

public enum StreamComprehender implements Comprehender<Stream> {
    INSTANCE;
    @Override
    public Class getTargetClass() {
        return Stream.class;
    }

    @Override
    public Object filter(final Stream t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final Stream t, final Function fn) {
        return t.map(fn);
    }

    @Override
    public Stream executeflatMap(final Stream t, final Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypes(this, fn.apply(input)));
    }

    @Override
    public Stream flatMap(final Stream t, final Function fn) {
        return t.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof Stream;
    }

    @Override
    public Stream empty() {
        return Stream.of();
    }

    @Override
    public Stream of(final Object o) {
        return Stream.of(o);
    }

    @Override
    public Stream fromIterator(final Iterator it) {
        return StreamUtils.stream(it);
    }

    public static <T> T unwrapOtherMonadTypes(final Comprehender<T> comp, final Object apply) {

        if (apply instanceof Collection) {
            return (T) ((Collection) apply).stream();
        }
        if (apply instanceof Iterable) {

            return (T) StreamUtils.stream((Iterable) apply);
        }
        if (apply instanceof BaseStream) {
            return (T) StreamSupport.stream(Spliterators.spliteratorUnknownSize(((BaseStream) apply).iterator(), Spliterator.ORDERED), false);
        }
        return Comprehender.unwrapOtherMonadTypes(comp, apply);

    }

}
