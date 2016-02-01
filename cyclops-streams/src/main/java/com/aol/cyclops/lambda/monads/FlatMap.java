package com.aol.cyclops.lambda.monads;

import java.util.function.Function;

public interface FlatMap<T> {

	public <R> FlatMap<R> flatMap(Function<? super T, ? extends FlatMap<R>> mapper);
	public <R> FlatMap<R> flatten();
}
