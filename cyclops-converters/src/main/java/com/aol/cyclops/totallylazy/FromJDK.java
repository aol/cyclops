package com.aol.cyclops.totallylazy;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Option;



public class FromJDK<T,R> {
	
	public static <T,R>  com.googlecode.totallylazy.Function<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  Function2<T,X,R> λ2(BiFunction<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> Option<T> option(java.util.Optional<T> o){
		return Option.option(o.orElse(null));
	}
	
}
