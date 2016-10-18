package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.seq.CompletableFutureTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;

public class CompletableFutureTSeqComprehender implements Comprehender<CompletableFutureTSeq>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final CompletableFutureTSeq apply) {

        return apply.isSeqPresent() ? comp.of(apply.stream()
                                                   .toListX())
                : comp.empty();
    }

    @Override
    public Object filter(final CompletableFutureTSeq t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final CompletableFutureTSeq t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final CompletableFutureTSeq t, final Function fn) {
        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public CompletableFutureTSeq of(final Object o) {
        return CompletableFutureTSeq.of(CompletableFuture.completedFuture(o));
    }

    @Override
    public CompletableFutureTSeq empty() {
        return CompletableFutureTSeq.emptyList();
    }

    @Override
    public Class getTargetClass() {
        return CompletableFutureTSeq.class;
    }

    @Override
    public CompletableFutureTSeq fromIterator(final Iterator o) {
        return CompletableFutureTSeq.of(FutureW.fromIterable(() -> o)
                                               .toCompletableFuture());
    }

}
