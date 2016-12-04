package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public enum OptionalComprehender implements ValueComprehender<Optional> {
    INSTANCE;
   
    @Override
    public Class getTargetClass() {
        return Optional.class;
    }

    @Override
    public Object filter(final Optional o, final Predicate p) {
        return o.filter(p);
    }

    @Override
    public Object map(final Optional o, final Function fn) {
        return o.map(fn);
    }

    @Override
    public Optional flatMap(final Optional o, final Function fn) {
        return o.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof Optional;
    }

    @Override
    public Optional of(final Object o) {
        return Optional.of(o);
    }

    @Override
    public Optional empty() {
        return Optional.empty();
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Optional apply) {
        if (apply.isPresent())
            return comp.of(apply.get());
        else
            return comp.empty();
    }

}
