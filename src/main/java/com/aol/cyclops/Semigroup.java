package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface Semigroup<T> extends BinaryOperator<T>{
	default BiFunction<T,T,T> combiner(){
	    return this;
	}
	
	
	
	@Override
    T apply(T t, T u);



    default BinaryOperator<T> reducer(){
		return (a,b) -> combiner().apply(a,b);
	}
}
