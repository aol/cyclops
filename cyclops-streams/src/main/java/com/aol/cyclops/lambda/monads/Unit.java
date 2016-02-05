package com.aol.cyclops.lambda.monads;

public interface Unit<T> {

	public <T> Unit<T> unit(T unit);
	
}
