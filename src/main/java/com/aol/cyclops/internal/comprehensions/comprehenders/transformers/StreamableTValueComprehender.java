package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class StreamableTValueComprehender implements Comprehender<StreamableTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final StreamableTValue apply) {

        return apply.isStreamablePresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final StreamableTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final StreamableTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final StreamableTValue t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public StreamableTValue of(final Object o) {
        return StreamableTValue.of(Streamable.of(o));
    }

    @Override
    public StreamableTValue empty() {
        return StreamableTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return StreamableTValue.class;
    }

    @Override
    public StreamableTValue fromIterator(final Iterator o) {
        return StreamableTValue.of(Streamable.fromIterable(() -> o));
    }

}
