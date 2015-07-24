package com.aol.cyclops.guava;

import java.util.function.Function;

import com.google.common.base.Optional;

public class FromJDK<T,R> {
	
	public static <T,R>  com.google.common.base.Function<T,R> f1(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	
	public static<T> Optional<T> option(java.util.Optional<T> o){
		if(o.isPresent())
			return Optional.of(o.get());
		return Optional.absent();
		
	}
	
}
