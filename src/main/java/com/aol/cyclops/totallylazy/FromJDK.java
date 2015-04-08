package com.aol.cyclops.totallylazy;

import javaslang.Functions.λ1;
import javaslang.Functions.λ2;
import javaslang.monad.Option;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FromJDK<T,R> {
	
	public static <T,R>  λ1<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  λ2<T,X,R> λ2(BiFunction<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> Option<T> option(java.util.Optional<T> o){
		return Option.of(o.orElse(null));
	}
	
}
