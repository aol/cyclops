package com.aol.cyclops.lambda.types;



@FunctionalInterface
public interface Unit<T>{

	public <T> Unit<T> unit(T unit);
	
	
	
}
