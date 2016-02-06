package com.aol.cyclops.lambda.monads;



@FunctionalInterface
public interface Unit<T>{

	public <T> Unit<T> unit(T unit);
	
	
	
}
