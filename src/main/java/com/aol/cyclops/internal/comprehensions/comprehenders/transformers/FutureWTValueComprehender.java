package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;

public class FutureWTValueComprehender implements ValueComprehender<FutureWTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final FutureWTValue apply) {

        return apply.isFuturePresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final FutureWTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final FutureWTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final FutureWTValue t, final Function fn) {

        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public FutureWTValue of(final Object o) {
        return FutureWTValue.of(FutureW.ofResult(o));
    }

    @Override
    public FutureWTValue empty() {
        return FutureWTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return FutureWTValue.class;
    }

}
