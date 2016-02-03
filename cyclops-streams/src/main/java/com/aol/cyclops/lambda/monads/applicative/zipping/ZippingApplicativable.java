package com.aol.cyclops.lambda.monads.applicative.zipping;

import com.aol.cyclops.lambda.monads.IterableFunctor;

public interface ZippingApplicativable<T> extends IterableFunctor<T>{

	default <R> IterableFunctor<R> ap1( ZippingApplicative<T,R, ?> ap){
		return ap.ap(this);
	}
	default <T2,R> ZippingApplicative<T2, R, ?> ap2( ZippingApplicative2<T,T2,R, ?> ap2){
		return ap2.ap(this);
	}
	
}
