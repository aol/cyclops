package com.aol.cyclops.lambda.api;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.monads.Monad;
import com.aol.cyclops.lambda.monads.MonadWrapper;

public class AsGenericMonad {

	/**
	 * Create a duck typed Monad. 
	 * Monaad should have methods
	 * 
	 * map(F f)
	 * filter(P p)
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 * 
	 * @param monad to wrap
	 * @return Duck typed Monad
	 */
	public static <T,MONAD> Monad<T,MONAD> asMonad(Object monad){
		return new MonadWrapper<>(monad);
	}
	public static <T,MONAD> Monad<T,MONAD> convertToMonad(Object monad){
		return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(monad));
	}
}
