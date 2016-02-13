package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public interface Semigroup<T> {
	BiFunction<T,T,T> combiner();
	
	default BinaryOperator<T> reducer(){
		return (a,b) -> combiner().apply(a,b);
	}
}
