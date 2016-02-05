package com.aol.cyclops.lambda.monads;

public interface EmptyUnit<T> extends Unit<T> {
	public <T> Unit<T> emptyUnit();
}
