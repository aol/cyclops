package com.aol.cyclops.lambda.monads.applicative;

import com.aol.cyclops.lambda.monads.Functor;

public interface Applicativable<T> extends Functor<T>{

	default <R> Functor<R> ap1( Applicative<T,R, ?> ap){
		return ap.ap(this);
	}
	default <T2,R> Applicative<T,R, ?> ap2( Applicative2<T,T2,R, ?> ap2){
		return ap2.ap(this);
	}
}
