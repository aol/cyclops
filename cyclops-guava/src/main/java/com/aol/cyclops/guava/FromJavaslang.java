package com.aol.cyclops.guava;



import javaslang.Function1;
import javaslang.control.Option;

import com.google.common.base.Function;
import com.google.common.base.Optional;


public class FromJavaslang {
	public static <T,R>  Function<T,R> f1(Function1<T,R> fn){
		return (t) -> fn.apply(t);
	}
	
	public static<T> Optional<T> option(Option<T> o){
		if(o.isEmpty())
			return Optional.absent();
		return Optional.of(o.get());
	}
	
	
}