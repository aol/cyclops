package com.aol.cyclops.javaslang;

import javaslang.Functions.λ1;
import javaslang.monad.Option;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class FromGuava {
	public static <T,R>  λ1<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static<T> Option<T> option(Optional<T> o){
		return Option.of(o.orNull());
	}
}