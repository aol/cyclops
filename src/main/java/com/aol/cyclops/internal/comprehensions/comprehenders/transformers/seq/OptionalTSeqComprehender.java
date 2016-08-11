package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class OptionalTSeqComprehender implements Comprehender<OptionalTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, OptionalTSeq apply) {

        return apply.isSeqPresent() ? comp.of(apply.stream()
                                                   .toListX())
                : comp.empty();
    }

    @Override
    public Object filter(OptionalTSeq t, Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(OptionalTSeq t, Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(OptionalTSeq t, Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public OptionalTSeq of(Object o) {
        return OptionalTSeq.of(Optional.of(o));
    }

    @Override
    public OptionalTSeq empty() {
        return OptionalTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return OptionalTSeq.class;
    }

    @Override
    public OptionalTSeq fromIterator(Iterator o) {
        return OptionalTSeq.of(Maybe.fromIterable(() -> o)
                                    .toOptional());
    }

}
