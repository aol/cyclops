package com.aol.cyclops.internal.monads;

import lombok.extern.java.Log;



public interface AnyMForComprehensionFactory {

	public <T> AnyMForComprehensionHandler<T> create();
	
	
	public final static AnyMForComprehensionHandler instance = MetaFactory.get();
	@Log
	static class MetaFactory{
		static  AnyMForComprehensionHandler get(){
			try {
				return (AnyMForComprehensionHandler)Class.forName("com.aol.cyclops.comprehensions.anyM.AnyMForComprehensions").newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				return null;
			}
		}
	}
}
