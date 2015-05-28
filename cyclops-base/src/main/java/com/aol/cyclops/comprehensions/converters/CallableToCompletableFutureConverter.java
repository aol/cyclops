package com.aol.cyclops.comprehensions.converters;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class CallableToCompletableFutureConverter implements MonadicConverter<CompletableFuture> {

	@Override
	public boolean accept(Object o) {
		return o instanceof Callable;
	}

	@Override
	public CompletableFuture convertToMonadicForm(Object f) {
		Supplier  s = ()-> {
			try {
				return ((Callable)f).call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		return CompletableFuture.supplyAsync(s);
	}

}
