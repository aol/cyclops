package com.aol.cyclops.functionaljava;

import java.util.function.BiFunction;
import java.util.function.Function;

import fj.F2;
import fj.F;
import fj.data.Option;

public class FromJDK<T,R> {
	
	public static <T,R>  F<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  F2<T,X,R> λ2(BiFunction<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> Option<T> option(java.util.Optional<T> o){
		if(o.isPresent())
			return Option.some(o.get());
		return Option.none();
		
	}
	
}
