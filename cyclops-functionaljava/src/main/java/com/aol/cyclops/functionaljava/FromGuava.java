package com.aol.cyclops.functionaljava;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import fj.F;

import fj.data.Option;

public class FromGuava {
	public static <T,R>  F<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static<T> Option<T> option(Optional<T> o){
		if(o.isPresent())
			return Option.some(o.get());
		return Option.none();
	}
}