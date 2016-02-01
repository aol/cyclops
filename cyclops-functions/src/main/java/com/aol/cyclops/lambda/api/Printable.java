package com.aol.cyclops.lambda.api;

/**
 * Mixin a print method with a return value (can be used as a method reference to
 * methods that accept Functions).
 * 
 * @author johnmcclean
 *
 */
public interface Printable {

	default <T> T print(T object){
		System.out.println(object);
		return object;
	}
	
}
