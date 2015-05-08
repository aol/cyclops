package com.aol.cyclops.comprehensions;

import java.util.function.Function;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MyComprehension<X>{
	private final Class<X> c;
	public <R> R foreach(Function<X,R> fn){
		return (R)new FreeFormForComprehension(c).foreach(fn);
	}
}