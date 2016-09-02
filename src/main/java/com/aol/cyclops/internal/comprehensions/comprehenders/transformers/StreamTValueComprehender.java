package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.values.StreamTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class StreamTValueComprehender implements Comprehender<StreamTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, StreamTValue apply) {

        return apply.isStreamPresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(StreamTValue t, Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(StreamTValue t, Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(StreamTValue t, Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public StreamTValue of(Object o) {
        return StreamTValue.of(ReactiveSeq.of(o));
    }

    @Override
    public StreamTValue empty() {
        return StreamTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return StreamTValue.class;
    }

    @Override
    public StreamTValue fromIterator(Iterator o) {
        return StreamTValue.of(ReactiveSeq.fromIterable(() -> o));
    }

}
