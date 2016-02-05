package com.aol.cyclops.monad;

import java.util.function.Function;

public interface Reader<T,R> extends Function<T,R> {
	public <R1> Reader<T, R1> map(Function<? super R, ? extends R1> f2);

	public <R1> Reader<T, R1> flatMap(Function<? super R, ? extends Reader<T, R1>> f);
	
	default AnyM<R> anyM(){
		return AnyM.ofMonad(this);
	}
}
