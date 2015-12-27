package com.aol.cyclops.lambda.monads;

import java.util.function.Function;

import com.aol.cyclops.monad.AnyM;

public interface AnyMForComprehensionHandler<U> {
	public <R1, R> AnyM<R> forEachAnyM2(AnyM<U> anyM, Function<U, ? extends AnyM<R1>> monad, Function<U, Function<R1, R>> yieldingFunction);
	
	public <R1,R> AnyM<R> forEachAnyM2(AnyM<U> anyM, Function<U,? extends AnyM<R1>> monad, 
			Function<U, Function<R1, Boolean>> filterFunction,
				Function<U,Function<R1,R>> yieldingFunction );
}
