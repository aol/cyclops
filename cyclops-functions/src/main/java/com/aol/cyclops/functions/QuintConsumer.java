package com.aol.cyclops.functions;

import java.util.function.Consumer;
import java.util.function.Function;

public interface QuintConsumer<T1, T2, T3, T4, T5> {

	public void accept(T1 a,T2 b, T3 c,T4 d,T5 e);
	
	default Function<T2,Function<T3,Function<T4,Consumer<T5>>>> apply(T1 s){
		return CurryConsumer.curry5(this).apply(s);
	}
	default Function<T3,Function<T4,Consumer<T5>>> apply(T1 s, T2 s2){
		return CurryConsumer.curry5(this).apply(s).apply(s2);
	}
	default Function<T4,Consumer<T5>> apply(T1 s, T2 s2, T3 s3){
		return CurryConsumer.curry5(this).apply(s).apply(s2).apply(s3);
	}
	default Consumer<T5> apply(T1 s, T2 s2, T3 s3,T4 s4){
		return CurryConsumer.curry5(this).apply(s).apply(s2).apply(s3).apply(s4);
	}
}
