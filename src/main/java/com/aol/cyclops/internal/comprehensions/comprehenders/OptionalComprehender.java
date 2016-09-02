package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class OptionalComprehender implements ValueComprehender<Optional> {
    public Class getTargetClass() {
        return Optional.class;
    }

    @Override
    public Object filter(Optional o, Predicate p) {
        return o.filter(p);
    }

    @Override
    public Object map(Optional o, Function fn) {
        return o.map(fn);
    }

    @Override
    public Optional flatMap(Optional o, Function fn) {
        return o.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(Object apply) {
        return apply instanceof Optional;
    }

    @Override
    public Optional of(Object o) {
        return Optional.of(o);
    }

    @Override
    public Optional empty() {
        return Optional.empty();
    }

    public Object resolveForCrossTypeFlatMap(Comprehender comp, Optional apply) {
        if (apply.isPresent())
            return comp.of(apply.get());
        else
            return comp.empty();
    }

}
