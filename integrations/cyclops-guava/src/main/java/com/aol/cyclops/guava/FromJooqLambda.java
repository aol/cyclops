package com.aol.cyclops.guava;

import org.jooq.lambda.function.Function1;

import com.google.common.base.Function;

public class FromJooqLambda{
	public static <T,R>  Function<T,R> f1(Function1<T,R> fn){
		return (t) -> fn.apply(t);
	}
	
	
	
}