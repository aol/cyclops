package com.aol.cyclops.internal.monads;

import lombok.extern.java.Log;



public interface AnyMForComprehensionFactory {

	public <T> AnyMForComprehensionHandler<T> create();
	
	
	public final static AnyMForComprehensionHandler instance =new  AnyMForComprehensions();
	
}
