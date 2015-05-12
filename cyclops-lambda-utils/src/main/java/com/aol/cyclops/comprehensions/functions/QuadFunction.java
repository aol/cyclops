package com.aol.cyclops.comprehensions.functions;

import java.util.function.Function;

public interface QuadFunction<T1, T2, T3, T4, R> {

	public R apply(T1 a,T2 b, T3 c,T4 d);
	
	default Function<T2,Function<T3,Function<T4,R>>> apply(T1 s){
		return Curry.curry4(this).apply(s);
	}
	default Function<T3,Function<T4,R>> apply(T1 s,T2 s2){
		return Curry.curry4(this).apply(s).apply(s2);
	}
	default Function<T4,R> apply(T1 s,T2 s2,T3 s3){
		return Curry.curry4(this).apply(s).apply(s2).apply(s3);
	}
}
