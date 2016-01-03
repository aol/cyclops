package com.aol.cyclops.lambda.monads;

import java.util.function.Function;

import com.aol.cyclops.monad.AnyM;

public interface AnyMForComprehensionHandler<U> {
	<R1, R> AnyM<R> forEach2(AnyM<U> anyM, Function<U, ? extends AnyM<R1>> monad, Function<U, Function<R1, R>> yieldingFunction);
	
	<R1,R> AnyM<R> forEach2(AnyM<U> anyM, Function<U,? extends AnyM<R1>> monad, 
			Function<U, Function<R1, Boolean>> filterFunction,
				Function<U,Function<R1,R>> yieldingFunction );
	<R1, R2, R> AnyM<R> forEach3(AnyM<U> anyM, Function<U, ? extends AnyM<R1>> monad1, 	
					Function<U,Function<R1,? extends AnyM<R2>>> monad2,
			Function<U, Function<R1, Function<R2, Boolean>>> filterFunction, 
				Function<U, Function<R1, Function<R2, R>>> yieldingFunction);

	<R1, R2, R> AnyM<R> forEach3(AnyM<U> anyM, Function<U, ? extends AnyM<R1>> monad1, 	Function<U,Function<R1,? extends AnyM<R2>>> monad2,
			Function<U, Function<R1, Function<R2, R>>> yieldingFunction);
}
