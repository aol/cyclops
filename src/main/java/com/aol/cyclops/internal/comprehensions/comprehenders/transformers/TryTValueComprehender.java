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
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final TryTValue apply) {
        return apply.isSuccess() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final TryTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final TryTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final TryTValue t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public TryTValue of(final Object o) {

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
