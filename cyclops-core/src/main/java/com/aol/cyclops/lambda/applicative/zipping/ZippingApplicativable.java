package com.aol.cyclops.lambda.applicative.zipping;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.QuintFunction;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.lambda.monads.IterableFunctor;
import com.aol.cyclops.lambda.monads.Unit;

public interface ZippingApplicativable<T> extends IterableFunctor<T>,Unit<T>{
	
	/**
	default <R,B> ZippingApplicativeBuilder<T,R,ZippingApplicativable<R>> applicatives(){
		return new ZippingApplicativeBuilder<T,R,ZippingApplicativable<R>> (this);
	}**/
	default <R> ApplyingZippingApplicativeBuilder<T,R,ZippingApplicativable<R>> applicatives(){
		return new ApplyingZippingApplicativeBuilder<T,R,ZippingApplicativable<R>> (this,this);
	}
	default <R> ZippingApplicativable<R> ap1(Function<? super T,? extends R> fn){
		return this.<R>applicatives().applicative(fn).ap(this);
		
	}
	default <T2,R> ZippingApplicative<T2,R, ?> ap2( BiFunction<? super T,? super T2,? extends R> fn){
		return this.<R>applicatives().applicative2(fn);
	}
	default <T2,T3,R> ZippingApplicative2<T2,T3,R, ?> ap3( TriFunction<? super T,? super T2,? super T3,? extends R> fn){
		return this.<R>applicatives().applicative3(fn);
	}
	default <T2,T3,T4,R> ZippingApplicative3<T2,T3,T4,R, ?> ap4( QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
		return this.<R>applicatives().applicative4(fn);
	}
	default <T2,T3,T4,T5,R> ZippingApplicative4<T2,T3,T4,T5,R, ?> ap5( QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
		return this.<R>applicatives().applicative5(fn);
	}
	default <R> IterableFunctor<R> ap1( ZippingApplicative<T,R, ?> ap){
		return ap.ap(this);
	}
	/**
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
	**/
}
