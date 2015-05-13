package com.aol.cyclops.lambda.monads;

import lombok.Value;
import lombok.experimental.Wither;

@Value
public class LiftableWrapper<T,A> implements LiftableFunctor<T, A, LiftableFunctor<T,?,?>>{
	@Wither
	Object functor;
	public Object getFunctor(){
		return functor;
	}
	public <T> Functor<T> withFunctor(Object functor){
		return new LiftableWrapper(functor);
	}
}
