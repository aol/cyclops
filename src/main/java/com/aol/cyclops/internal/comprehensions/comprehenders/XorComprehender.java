package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class XorComprehender implements ValueComprehender<Xor> {

    @Override
    public Object filter(final Xor t, final Predicate p) {
        return t.filter(x -> p.test(x));
    }

    @Override
    public Object map(final Xor t, final Function fn) {
        return t.map(e -> fn.apply(e));
    }

    @Override
    public Object flatMap(final Xor t, final Function fn) {
        return t.flatMap(e -> fn.apply(e));
    }

    @Override
    public Xor of(final Object o) {
        return Xor.primary(o);
    }

    @Override
    public Xor empty() {
        return Xor.secondary(null);
    }

    @Override
    public Class getTargetClass() {
        return Xor.class;
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Xor apply) {
        if (apply.isPrimary())
            return comp.of(apply.get());
        return comp.empty();
    }
}
