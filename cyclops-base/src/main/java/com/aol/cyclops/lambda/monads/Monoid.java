package com.aol.cyclops.lambda.monads;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public interface Monoid<T> {

	T zero();
	BiFunction<T,T,T> combiner();
	default BinaryOperator<T> reducer(){
		return (a,b) -> combiner().apply(a,b);
	}
	public static <T> Monoid<T> of(T zero, BiFunction<T,T,T> combiner){
		return new Monoid<T>(){
			public T zero(){
				return zero;
			}
			public BiFunction<T,T,T> combiner(){
				return combiner;
			}
		};
	}
}
