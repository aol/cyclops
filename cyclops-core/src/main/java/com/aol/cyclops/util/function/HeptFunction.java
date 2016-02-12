package com.aol.cyclops.util.function;

import java.util.function.Function;

public interface HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> {

	public R apply(T1 a,T2 b, T3 c,T4 d,T5 e,T6 f,T7 g);
	
	default Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>> apply(T1 s){
		return Curry.curry7(this).apply(s);
	}
	default Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>> apply(T1 s, T2 s2){
		return Curry.curry7(this).apply(s).apply(s2);
	}
	default Function<T4,Function<T5,Function<T6,Function<T7,R>>>> apply(T1 s, T2 s2, T3 s3){
		return Curry.curry7(this).apply(s).apply(s2).apply(s3);
	}
	default Function<T5,Function<T6,Function<T7,R>>> apply(T1 s, T2 s2, T3 s3,T4 s4){
		return Curry.curry7(this).apply(s).apply(s2).apply(s3).apply(s4);
	}
	default Function<T6,Function<T7,R>> apply(T1 s, T2 s2, T3 s3,T4 s4,T5 s5){
		return Curry.curry7(this).apply(s).apply(s2).apply(s3).apply(s4).apply(s5);
	}
	default Function<T7,R> apply(T1 s, T2 s2, T3 s3,T4 s4,T5 s5,T6 s6){
		return Curry.curry7(this).apply(s).apply(s2).apply(s3).apply(s4).apply(s5).apply(s6);
	}
}
