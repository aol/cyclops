package com.aol.cyclops.lambda.api;

public interface Printable {

	default <T> T print(T object){
		System.out.println(object);
		return object;
	}
	
}
