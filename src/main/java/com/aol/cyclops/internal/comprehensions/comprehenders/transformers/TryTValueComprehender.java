package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;

public class TryTValueComprehender implements ValueComprehender<TryTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, TryTValue apply) {
        return apply.isSuccess() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(TryTValue t, Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(TryTValue t, Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(TryTValue t, Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public TryTValue of(Object o) {

        return TryTValue.of(Try.success(o));
    }

    @Override
    public TryTValue empty() {
        return TryTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return TryTValue.class;
    }

}
