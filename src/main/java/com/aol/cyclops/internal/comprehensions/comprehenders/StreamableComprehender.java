package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.types.extensability.Comprehender;

public class StreamableComprehender implements Comprehender {
    @Override
    public Class getTargetClass() {
        return Streamable.class;
    }

    @Override
    public int priority() {
        return 500;
    }

    @Override
    public Object filter(final Object t, final Predicate p) {
        return ((Streamable) t).stream()
                               .filter(p);
    }

    @Override
    public Object map(final Object t, final Function fn) {
        return ((Streamable) t).stream()
                               .map(fn);
    }

    @Override
    public Object executeflatMap(final Object t, final Function fn) {
        return Streamable.fromStream((Stream) flatMap(t, input -> StreamComprehender.unwrapOtherMonadTypes(this, fn.apply(input))));
    }

    @Override
    public Object flatMap(final Object t, final Function fn) {
        return ((Streamable) t).stream()
                               .flatMap(fn);
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

}
