package com.aol.cyclops.lambda.types.applicative;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.QuintFunction;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.lambda.types.ConvertableFunctor;
import com.aol.cyclops.lambda.types.Functor;
import com.aol.cyclops.lambda.types.Unit;

public interface Applicativable<T> extends ConvertableFunctor<T>, Unit<T>{

	
	public static class Applicatives{
		public static <T,R> ApplyingApplicativeBuilder<T,R,Applicativable<R>> applicatives(Unit unit,Functor functor){
			return new ApplyingApplicativeBuilder<T,R,Applicativable<R>> (unit,functor);
		}
	}
	
	default <R> Applicativable<R> ap1(Function<? super T,? extends R> fn){
		return Applicatives.<T,R>applicatives(this,this).applicative(fn).ap(this);
		
	}
	/**
	 * Apply the provided function to two different Applicatives. e.g. given a method add
	 * 
	 * <pre>
	 * {@code 
	 * 	public int add(Integer a,Integer b){
	 * 			return a+b;
	 * 	}
	 * 
	 * }
	 * We can add two Applicative types together without unwrapping the values
	 * 
	 * <pre>
	 * {@code 
	 *  Maybe.of(10).ap2(this::add).ap(Maybe.of(20))
	 *  
	 *  //Maybe[30];
	 *  }
	 *  </pre>
	 * 
	 * @param fn
	 * @return
	 */
	default <T2,R> Applicative<T2,R, ?> ap2( BiFunction<? super T,? super T2,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative2(fn);
	}
	default <T2,T3,R> Applicative2<T2,T3,R, ?> ap3( TriFunction<? super T,? super T2,? super T3,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative3(fn);
	}
	default <T2,T3,T4,R> Applicative3<T2,T3,T4,R, ?> ap4( QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative4(fn);
	}
	default <T2,T3,T4,T5,R> Applicative4<T2,T3,T4,T5,R, ?> ap5( QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative5(fn);
	}
	/**
	default <R> Functor<R> ap1( Applicative<T,R, ?> ap){
		return ap.ap(this);
	}
	**/
	
	/**
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
	**/
	
}
