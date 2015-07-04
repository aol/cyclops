package com.aol.cyclops.javaslang;

import java.util.function.BiFunction;
import java.util.function.Function;

import javaslang.Function1;
import javaslang.Function2;
import javaslang.control.Option;

public class FromJDK<T,R> {
	
	public static <T,R>  Function1<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  Function2<T,X,R> λ2(BiFunction<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> Option<T> option(java.util.Optional<T> o){
		return Option.of(o.orElse(null));
	}
	
}
