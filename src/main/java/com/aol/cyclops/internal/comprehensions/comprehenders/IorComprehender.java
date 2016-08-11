package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Ior;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class IorComprehender implements ValueComprehender<Ior> {

    public Object filter(Ior t, Predicate p) {
        return t.filter(x -> p.test(x));
    }

    @Override
    public Object map(Ior t, Function fn) {
        return t.map(e -> fn.apply(e));
    }

    @Override
    public Object flatMap(Ior t, Function fn) {
        return t.flatMap(e -> fn.apply(e));
    }

    @Override
    public Ior of(Object o) {
        return Ior.primary(o);
    }

    @Override
    public Ior empty() {
        return Ior.secondary(null);
    }

    @Override
    public Class getTargetClass() {
        return Ior.class;
    }

    public Object resolveForCrossTypeFlatMap(Comprehender comp, Ior apply) {
        if (apply.isPrimary())
            return comp.of(apply.get());
        return comp.empty();
    }
}
