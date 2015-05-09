package com.aol.cyclops.comprehensions.converters;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SupplierToCompletableFutureConverter implements MonadicConverter<CompletableFuture> {

	@Override
	public boolean accept(Object o) {
		return o instanceof Supplier;
	}

	@Override
	public CompletableFuture convertToMonadicForm(Object f) {
		
		return CompletableFuture.supplyAsync((Supplier)f);
	}

}
