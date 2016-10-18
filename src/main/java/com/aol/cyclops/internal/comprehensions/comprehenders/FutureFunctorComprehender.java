package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class FutureFunctorComprehender implements ValueComprehender<FutureW> {
    @Override
    public Class getTargetClass() {
        return FutureW.class;
    }

    @Override
    public Object map(final FutureW t, final Function fn) {
        return t.map(fn);
    }

    @Override
    public Object filter(final FutureW t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public FutureW flatMap(final FutureW t, final Function fn) {
        return t.flatMap(fn);
    }

    @Override
    public boolean instanceOfT(final Object apply) {
        return apply instanceof FutureW;
    }

    @Override
    public FutureW of(final Object o) {
        return FutureW.of(CompletableFuture.completedFuture(o));
    }

    @Override
    public FutureW empty() {
        return FutureW.ofResult(null);
    }

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final FutureW apply) {
        final Xor<Throwable, ?> res = apply.toXor();
        return res.isPrimary() ? comp.of(res.get()) : comp.empty();
    }

}
