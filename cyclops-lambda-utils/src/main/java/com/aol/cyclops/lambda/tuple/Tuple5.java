package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

public interface Tuple5<T1,T2,T3,T4,T5> extends Tuple4<T1,T2,T3,T4> {
	
	default T5 v5(){
		return (T5)getCachedValues().get(4);
	}
	default T5 _5(){
		return v5();
	}

	default T5 getT5(){
		return v5();
	}
	default int arity(){
		return 5;
	}
	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> ofTuple(Object tuple5){
		return (Tuple5)new Tuples(tuple5);
	}
	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5){
		return (Tuple5)new Tuples(Arrays.asList(t1,t2,t3,t4,t5));
	}
}
