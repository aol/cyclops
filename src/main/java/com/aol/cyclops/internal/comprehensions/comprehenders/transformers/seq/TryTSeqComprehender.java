package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.seq.TryTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class TryTSeqComprehender implements Comprehender<TryTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final TryTSeq apply) {

        return apply.isSeqPresent() ? comp.of(apply.stream()
                                                   .toListX())
                : comp.empty();
    }

    @Override
    public Object filter(final TryTSeq t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final TryTSeq t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final TryTSeq t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public TryTSeq of(final Object o) {
        return TryTSeq.of(Try.success(o));
    }

    @Override
    public TryTSeq empty() {
        return TryTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return TryTSeq.class;
    }

    @Override
    public TryTSeq fromIterator(final Iterator o) {
        return TryTSeq.of(Try.fromIterable(() -> o));
    }

}
