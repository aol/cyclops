package com.aol.cyclops.lambda.monads;

import lombok.Value;
import lombok.experimental.Wither;

@Value
public class FunctorWrapper<T> implements WrappingFunctor<T>{
	@Wither
	Object functor;
	public Object getFunctor(){
		return functor;
	}
	public <T> WrappingFunctor<T> withFunctor(T functor){
		return new FunctorWrapper(functor);
	}
}
