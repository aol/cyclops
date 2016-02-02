package com.aol.cyclops.lambda.monads;

public interface FlatMap<T> extends Unit<T>,Functor<T> {

	public <R> FlatMap<R> flatten();
	
}
