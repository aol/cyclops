package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public interface Semigroup<T> extends BinaryOperator<T>{
	BiFunction<T,T,T> combiner();
	
	
	
	@Override
    default T apply(T t, T u) {
       return combiner().apply(t, u);
    }



    default BinaryOperator<T> reducer(){
		return (a,b) -> combiner().apply(a,b);
	}
}
