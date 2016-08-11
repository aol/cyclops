package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class EvalTSeqComprehender implements Comprehender<EvalTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, EvalTSeq apply) {

        return apply.isSeqPresent() ? comp.of(apply.stream()
                                                   .toListX())
                : comp.empty();
    }

    @Override
    public Object filter(EvalTSeq t, Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(EvalTSeq t, Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(EvalTSeq t, Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public EvalTSeq of(Object o) {
        return EvalTSeq.of(Eval.now(o));
    }

    @Override
    public EvalTSeq empty() {
        return EvalTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return EvalTSeq.class;
    }

    @Override
    public EvalTSeq fromIterator(Iterator o) {
        return EvalTSeq.of(Eval.fromIterable(() -> o));
    }

}
