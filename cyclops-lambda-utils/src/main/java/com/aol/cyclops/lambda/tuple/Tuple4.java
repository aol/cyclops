package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

public interface Tuple4<T1,T2,T3,T4> extends Tuple3<T1,T2,T3> {
	
	default T4 v4(){
		return (T4)getCachedValues().get(3);
	}
	default T4 _4(){
		return v4();
	}

	default T4 getT4(){
		return v4();
	}
	
	default int arity(){
		return 4;
	}
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> ofTuple(Object tuple4){
		return (Tuple4)new Tuples(tuple4);
	}
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> of(T1 t1, T2 t2,T3 t3,T4 t4){
		return (Tuple4)new Tuples(Arrays.asList(t1,t2,t3,t4));
	}
}
