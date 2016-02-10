package com.aol.cyclops.guava;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import fj.F;


public class FromFunctionalJava {
	public static <T,R>  Function<T,R> f1(F<T,R> fn){
		return (t) -> fn.f(t);
	}
	
	public static<T> Optional<T> option(fj.data.Option<T> o){
		if(o.isNone())
			return Optional.absent();
		return Optional.of(o.some());
	}
	
}