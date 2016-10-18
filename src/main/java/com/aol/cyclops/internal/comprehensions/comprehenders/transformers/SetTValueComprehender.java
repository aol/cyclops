package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class SetTValueComprehender implements Comprehender<SetTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final SetTValue apply) {

        return apply.isSetPresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final SetTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final SetTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final SetTValue t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public SetTValue of(final Object o) {
        return SetTValue.of(SetX.of(o));
    }

    @Override
    public SetTValue empty() {
        return SetTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return SetTValue.class;
    }

    @Override
    public SetTValue fromIterator(final Iterator o) {
        return SetTValue.of(SetX.fromIterable(() -> o));
    }

}
