package com.aol.cyclops.lambda.monads.applicative.zipping;

import com.aol.cyclops.lambda.monads.IterableFunctor;

public interface ZippingApplicativable<T> extends IterableFunctor<T>{

	default <R> IterableFunctor<R> ap1( ZippingApplicative<T,R, ?> ap){
		return ap.ap(this);
	}
	default <T2,R> ZippingApplicative<T2, R, ?> ap2( ZippingApplicative2<T,T2,R, ?> ap2){
		return ap2.ap(this);
	}
	default <T2,T3,R> ZippingApplicative2<T2,T3, R, ?> ap2( ZippingApplicative3<T,T2,T3,R, ?> ap3){
		return ap3.ap(this);
	}
	default <T2,T3,T4,R> ZippingApplicative3<T2,T3, T4,R, ?> ap2( ZippingApplicative4<T,T2,T3,T4,R, ?> ap4){
		return ap4.ap(this);
	}
	default <T2,T3,T4,T5,R> ZippingApplicative4<T2,T3, T4,T5,R, ?> ap2( ZippingApplicative5<T,T2,T3,T4,T5,R, ?> ap5){
		return ap5.ap(this);
	}
	
}
