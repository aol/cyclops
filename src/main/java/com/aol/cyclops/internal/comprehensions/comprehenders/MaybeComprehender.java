package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public enum MaybeComprehender implements ValueComprehender<Maybe> {
    INSTANCE;
    @Override
    public Class getTargetClass() {
        return Maybe.class;
    }

    @Override
    public Object filter(final Maybe o, final Predicate p) {
        return o.filter(p);
    }

    @Override
    public Object map(final Maybe o, final Function fn) {
        return o.map(fn);
    }

    @Override
    public Maybe flatMap(final Maybe o, final Function fn) {
        return o.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof Maybe;
    }

    @Override
    public Maybe of(final Object o) {
        return Maybe.of(o);
    }

    @Override
    public Maybe empty() {
        return Maybe.none();
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Maybe apply) {
        if (apply.isPresent())
            return comp.of(apply.get());
        else
            return comp.empty();
    }

}
