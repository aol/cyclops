package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

public interface Tuple2<T1,T2> extends Tuple1<T1> {
	
	default T2 v2(){
		return (T2)getCachedValues().get(1);
	}
	default T2 _2(){
		return v2();
	}

	default T2 getT2(){
		return v2();
	}
	
	default int arity(){
		return 2;
	}
	public static <T1,T2> Tuple2<T1,T2> ofTuple(Object tuple2){
		return (Tuple2)new Tuples(tuple2);
	}
	public static <T1,T2> Tuple2<T1,T2> of(T1 t1, T2 t2){
		return (Tuple2)new Tuples(Arrays.asList(t1,t2));
	}
}
