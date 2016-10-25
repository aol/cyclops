package com.aol.cyclops.internal.comprehensions.converters;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.aol.cyclops.types.extensability.MonadicConverter;

public class SupplierToCompletableFutureConverter implements MonadicConverter<CompletableFuture> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof Supplier;
    }

    @Override
    public CompletableFuture convertToMonadicForm(final Object f) {

        return CompletableFuture.supplyAsync((Supplier) f);
    }

}
