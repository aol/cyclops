package com.aol.cyclops.comprehensions;

import java.util.function.Function;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MyComprehension<X,V>{
	private final Class<X> c;
	private final Class<V> vars;
	public <R> R foreach(Function<X,R> fn){
		return (R)new FreeFormForComprehension(c,vars).foreach(fn);
	}
}