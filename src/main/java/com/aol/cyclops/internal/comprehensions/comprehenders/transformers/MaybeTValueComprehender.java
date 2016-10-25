package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;

public class MaybeTValueComprehender implements ValueComprehender<MaybeTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final MaybeTValue apply) {

        return apply.isPresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final MaybeTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final MaybeTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final MaybeTValue t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public MaybeTValue of(final Object o) {
        return MaybeTValue.of(Maybe.just(o));
    }

    @Override
    public MaybeTValue empty() {
        return MaybeTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return MaybeTValue.class;
    }

}
