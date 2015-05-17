package com.aol.cyclops.lambda.monads;

public class AsFunctor {
	public static <T> Functor<T> asFunctor(Object o){
		return new FunctorWrapper<>(o);
	}
	
}
