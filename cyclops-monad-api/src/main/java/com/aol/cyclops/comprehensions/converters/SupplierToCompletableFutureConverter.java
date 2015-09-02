package com.aol.cyclops.comprehensions.converters;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class SupplierToCompletableFutureConverter implements MonadicConverter<CompletableFuture> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof Supplier;
	}

	@Override
	public CompletableFuture convertToMonadicForm(Object f) {
		
		return CompletableFuture.supplyAsync((Supplier)f);
	}

}
