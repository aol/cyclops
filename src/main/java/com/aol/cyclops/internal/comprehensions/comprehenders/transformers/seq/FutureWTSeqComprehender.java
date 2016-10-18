package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class FutureWTSeqComprehender implements Comprehender<FutureWTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final FutureWTSeq apply) {

        return apply.isSeqPresent() ? comp.of(apply.stream()
                                                   .toListX())
                : comp.empty();
    }

    @Override
    public Object filter(final FutureWTSeq t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final FutureWTSeq t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final FutureWTSeq t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public FutureWTSeq of(final Object o) {
        return FutureWTSeq.of(FutureW.ofResult(o));
    }

    @Override
    public FutureWTSeq empty() {
        return FutureWTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return FutureWTSeq.class;
    }

    @Override
    public FutureWTSeq fromIterator(final Iterator o) {
        return FutureWTSeq.of(FutureW.fromIterable(() -> o));
    }

}
