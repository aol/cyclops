package com.aol.cyclops.internal.monads;

import java.util.function.Function;

import com.aol.cyclops.monad.AnyM;

public interface AnyMForComprehensionHandler<U> {
	<R1, R> AnyM<R> forEach2(AnyM<U> anyM, Function<? super U, ? extends AnyM<R1>> monad, Function<? super U, Function<? super R1, ? extends R>> yieldingFunction);
	
	
	<R1,R> AnyM<R> forEach2(AnyM<U> anyM, Function<? super U,? extends AnyM<R1>> monad, 
			Function<? super U, Function<? super R1, Boolean>> filterFunction,
				Function<? super U,Function<? super R1,? extends R>> yieldingFunction );
	<R1, R2, R> AnyM<R> forEach3(AnyM<U> anyM, Function<? super U, ? extends AnyM<R1>> monad1, 	
					Function<? super U,Function<? super R1,? extends AnyM<R2>>> monad2,
			Function<? super U, Function<? super R1, Function<? super R2, Boolean>>> filterFunction, 
				Function<? super U, Function<? super R1, Function<? super R2,? extends R>>> yieldingFunction);

	<R1, R2, R> AnyM<R> forEach3(AnyM<U> anyM, Function<? super U, ? extends AnyM<R1>> monad1, 	Function<? super U,Function<? super R1,? extends AnyM<R2>>> monad2,
			Function<? super U, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);
}
