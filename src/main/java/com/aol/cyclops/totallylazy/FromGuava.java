package com.aol.cyclops.totallylazy;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import javaslang.Functions.λ1;
import javaslang.monad.Option;

public class FromGuava {
	public static <T,R>  λ1<T,R> λ(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static<T> Option<T> option(Optional<T> o){
		return Option.of(o.orNull());
	}
}