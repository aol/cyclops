package com.aol.cyclops.lambda.monads;

import lombok.Value;
import lombok.experimental.Wither;

@Value
public class FunctorWrapper<T> implements Functor<T>{
	@Wither
	Object functor;
	public Object getFunctor(){
		return functor;
	}
	public <T> Functor<T> withFunctor(T functor){
		return new FunctorWrapper(functor);
	}
}
