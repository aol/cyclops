package com.aol.cyclops.monad;

import java.util.function.Function;

import lombok.Value;

import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.ConstructableFunctor;


@Value
public class FunctorWrapper<A> implements ConstructableFunctor<A,A,FunctorWrapper<A>>{
	
	A a;
	
	public static <A> Free<Functor<?>,A> liftF(A f){
		return Free.suspend(new FunctorWrapper(Free.ret(f)));
	}
	public <B> FunctorWrapper<B> map(Function<A,B> fn){
		return new FunctorWrapper(fn.apply(a));
	}
	

}
