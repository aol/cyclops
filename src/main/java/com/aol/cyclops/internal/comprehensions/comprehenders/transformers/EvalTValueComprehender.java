package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;

public class EvalTValueComprehender implements ValueComprehender<EvalTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final EvalTValue apply) {

        return apply.isValuePresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final EvalTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final EvalTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final EvalTValue t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public EvalTValue of(final Object o) {
        return EvalTValue.of(Eval.later(() -> o));
    }

    @Override
    public EvalTValue empty() {
        return EvalTValue.emptyMaybe();
    }

    @Override
    public Class getTargetClass() {
        return EvalTValue.class;
    }

}
