package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.futurestream.SimpleReactStream;

public class SimpleReactStreamComprehender implements Comprehender {

    public static int priority = 4;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Object filter(final Object t, final Predicate p) {
        return ((SimpleReactStream) t).filter(p);
    }

    @Override
    public Object map(final Object t, final Function fn) {
        return ((SimpleReactStream) t).then(fn);
    }

    @Override
    public SimpleReactStream flatMap(final Object in, final Function fn) {
        final SimpleReactStream t = (SimpleReactStream) in;

        return SimpleReactStream.bind(t, input -> (SimpleReactStream) unwrapOtherMonadTypes(t, this, fn.apply(input)));
    }

    @Override
    public SimpleReactStream of(final Object o) {
        return new SimpleReact().of(o);
    }

    @Override
    public SimpleReactStream fromIterator(final Iterator it) {
        return new SimpleReact().fromIterable(() -> it);
    }

    @Override
    public SimpleReactStream empty() {
        return new SimpleReact().of();
    }

    @Override
    public Class getTargetClass() {
        return SimpleReactStream.class;
    }

    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final SimpleReactStream apply) {
        return comp.of(apply.block());
    }

    static SimpleReactStream unwrapOtherMonadTypes(final SimpleReactStream t, final Comprehender<SimpleReactStream> comp, final Object apply) {

        if (apply instanceof Collection) {
            return t.fromStream(((Collection) apply).stream());
        }
        if (apply instanceof Iterable) {
            return t.fromStream(StreamSupport.stream(((Iterable) apply).spliterator(), false));
        }

        return Comprehender.unwrapOtherMonadTypes(comp, apply);

    }
}
