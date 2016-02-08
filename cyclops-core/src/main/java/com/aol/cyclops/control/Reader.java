package com.aol.cyclops.control;

import java.util.function.Function;

import com.aol.cyclops.monad.AnyM;

public interface Reader<T,R> extends Function<T,R> {
	public <R1> Reader<T, R1> map(Function<? super R, ? extends R1> f2);

	public <R1> Reader<T, R1> flatMap(Function<? super R, ? extends Reader<T, R1>> f);
	
	default AnyM<R> anyM(){
		return AnyM.ofMonad(this);
	}
}
