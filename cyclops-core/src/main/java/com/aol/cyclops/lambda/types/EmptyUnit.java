package com.aol.cyclops.lambda.types;

public interface EmptyUnit<T> extends Unit<T> {
	public <T> Unit<T> emptyUnit();
}
