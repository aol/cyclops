package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.types.extensability.Comprehender;

public class StreamableComprehender implements Comprehender {
    public Class getTargetClass() {
        return Streamable.class;
    }

    @Override
    public int priority() {
        return 500;
    }

    @Override
    public Object filter(Object t, Predicate p) {
        return ((Streamable) t).stream()
                               .filter(p);
    }

    @Override
    public Object map(Object t, Function fn) {
        return ((Streamable) t).stream()
                               .map(fn);
    }

    public Object executeflatMap(Object t, Function fn) {
        return Streamable.fromStream((Stream) flatMap(t, input -> StreamComprehender.unwrapOtherMonadTypes(this, fn.apply(input))));
    }

    @Override
    public Object flatMap(Object t, Function fn) {
        return ((Streamable) t).stream()
                               .flatMap(fn);
    }

    @Override
    public boolean instanceOfT(Object apply) {
        return apply instanceof Stream;
    }

    @Override
    public Stream empty() {
        return Stream.of();
    }

    @Override
    public Stream of(Object o) {
        return Stream.of(o);
    }

    public Stream fromIterator(Iterator it) {
        return StreamUtils.stream(it);
    }

}
