package com.aol.cyclops.lambda.api;

import com.aol.cyclops.lambda.monads.FunctorWrapper;
import com.aol.cyclops.lambda.monads.WrappingFunctor;
import com.aol.cyclops.lambda.types.Functor;

public class AsFunctor {
	
	/**
	 * Create a Duck typed functor. Wrapped class should have a method
	 * 
	 * map(F f)
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.
	 * 
	 * @param o functor to wrap
	 * @return Duck typed functor
	 */
	public static <T> WrappingFunctor<T> asFunctor(Object o){
		return new FunctorWrapper<>(o);
	}
	
}
