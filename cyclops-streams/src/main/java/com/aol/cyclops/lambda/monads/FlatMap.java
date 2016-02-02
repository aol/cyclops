package com.aol.cyclops.lambda.monads;

public interface FlatMap<T> {

	public <R> FlatMap<R> flatten();
}
