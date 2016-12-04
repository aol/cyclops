package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public enum CompletableFutureComprehender implements ValueComprehender<CompletableFuture> {
    INSTANCE;
    @Override
    public Class getTargetClass() {
        return CompletableFuture.class;
    }

    @Override
    public Object filter(final CompletableFuture t, final Predicate p) {
        return FutureW.of(t)
                      .filter(p);
    }

    @Override
    public Object map(final CompletableFuture t, final Function fn) {
        return t.thenApply(fn);
    }

    @Override
    public CompletableFuture flatMap(final CompletableFuture t, final Function fn) {
        return t.thenCompose(fn);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof CompletableFuture;
    }

    @Override
    public CompletableFuture of(final Object o) {
        return CompletableFuture.completedFuture(o);
    }

    @Override
    public CompletableFuture empty() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final CompletableFuture apply) {
        final Xor<Throwable, ?> res = FutureW.of(apply)
                                             .toXor();
        return res.isPrimary() ? comp.of(res.get()) : comp.empty();
    }

}
