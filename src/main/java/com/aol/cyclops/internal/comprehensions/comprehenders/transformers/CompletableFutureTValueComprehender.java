package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;

public class CompletableFutureTValueComprehender implements ValueComprehender<CompletableFutureTValue>, Printable {

    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final CompletableFutureTValue apply) {

        return apply.isFuturePresent() ? comp.of(apply.get()) : comp.empty();
    }

    @Override
    public Object filter(final CompletableFutureTValue t, final Predicate p) {
        return t.filter(p);
    }

    @Override
    public Object map(final CompletableFutureTValue t, final Function fn) {
        return t.map(r -> fn.apply(r));
    }

    @Override
    public Object flatMap(final CompletableFutureTValue t, final Function fn) {

        return t.flatMapT(r -> fn.apply(r));
    }

    @Override
    public CompletableFutureTValue of(final Object o) {

        return CompletableFutureTValue.of(CompletableFuture.completedFuture(o));
    }

    @Override
    public CompletableFutureTValue empty() {
        return CompletableFutureTValue.emptyOptional();
    }

    @Override
    public Class getTargetClass() {
        return CompletableFutureTValue.class;
    }

}
