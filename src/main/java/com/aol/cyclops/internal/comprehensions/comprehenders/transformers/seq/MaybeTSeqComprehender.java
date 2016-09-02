package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class MaybeTSeqComprehender implements Comprehender<MaybeTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, MaybeTSeq apply) {

        return apply.isSeqPresent() ? comp.of(apply.stream()
                                                   .toListX())
                : comp.empty();
    }

    @Override
    public Object filter(MaybeTSeq t, Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(MaybeTSeq t, Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(MaybeTSeq t, Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public MaybeTSeq of(Object o) {
        return MaybeTSeq.of(Maybe.of(o));
    }

    @Override
    public MaybeTSeq empty() {
        return MaybeTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return MaybeTSeq.class;
    }

    @Override
    public MaybeTSeq fromIterator(Iterator o) {
        return MaybeTSeq.of(Maybe.fromIterable(() -> o));
    }

}
