package com.aol.cyclops.monad;

import java.util.function.Function;

import lombok.Value;

import com.aol.cyclops.lambda.monads.Functor;


@Value
public class FunctorWrapper<A> implements Functor<A>{
	
	A a;
	
	public static <A> Free<Functor<?>,A> liftF(A f){
		return Free.suspend(new FunctorWrapper(Free.ret(f)));
	}
	public <B> FunctorWrapper<B> map(Function<? super A,? extends B> fn){
		return new FunctorWrapper(fn.apply(a));
	}
	

}
