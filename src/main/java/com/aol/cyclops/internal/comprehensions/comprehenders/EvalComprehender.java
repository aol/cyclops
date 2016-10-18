package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class EvalComprehender implements ValueComprehender<Eval> {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Eval apply) {
        final Maybe m = apply.toMaybe();
        return m.isPresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final Eval t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final Eval t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final Eval t, final Function fn) {
        return t.flatMap(r -> fn.apply(r));
    }

    @Override
    public Eval of(final Object o) {
        return Eval.later(() -> o);
    }

    @Override
    public Eval empty() {
        return Eval.later(() -> null);
    }

    @Override
    public Class getTargetClass() {
        return Eval.class;
    }

}
