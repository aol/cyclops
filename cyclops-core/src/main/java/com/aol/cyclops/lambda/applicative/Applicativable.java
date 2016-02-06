package com.aol.cyclops.lambda.applicative;

import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Unit;

public interface Applicativable<T> extends ConvertableFunctor<T>, Unit<T>{

	
	default <R> ApplicativeBuilder<T,R,Applicativable<R>> applicatives(){
		return new ApplicativeBuilder<T,R,Applicativable<R>> (this);
	}
	default <R> ApplyingApplicativeBuilder<T,R,Applicativable<R>> applicativesApplied(){
		return new ApplyingApplicativeBuilder<T,R,Applicativable<R>> (this,this);
	}
	default <R> Functor<R> ap1( Applicative<T,R, ?> ap){
		return ap.ap(this);
	}
	default <T2,R> Applicative<T2,R, ?> ap2( Applicative2<T,T2,R, ?> ap2){
		return ap2.ap(this);
	}
	default <T2,T3,R> Applicative2<T2,T3,R, ?> ap3( Applicative3<T,T2,T3,R, ?> ap3){
		return ap3.ap(this);
	}
	default <T2,T3,T4,R> Applicative3<T2,T3,T4,R, ?> ap4( Applicative4<T,T2,T3,T4,R, ?> ap4){
		return ap4.ap(this);
	}
	default <T2,T3,T4,T5,R> Applicative4<T2,T3,T4,T5,R, ?> ap4( Applicative5<T,T2,T3,T4,T5,R, ?> ap5){
		return ap5.ap(this);
	}
}
