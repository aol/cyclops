package com.aol.cyclops.lambda.monads;

public class AsGenericMonad {

	public static <T,MONAD> Monad<T,MONAD> asMonad(Object monad){
		return new MonadWrapper<>(monad);
	}
}
