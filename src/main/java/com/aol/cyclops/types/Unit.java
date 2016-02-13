package com.aol.cyclops.types;



@FunctionalInterface
public interface Unit<T>{

	public <T> Unit<T> unit(T unit);
	
	
	
}
