package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

interface Tuple1<T1> extends CachedValues{
	
	default T1 v1(){
		return (T1)getCachedValues().get(0);
	}
	default T1 _1(){
		return v1();
	}

	default T1 getT1(){
		return v1();
	}
	
	default int arity(){
		return 1;
	}

	public static <T1> Tuple1<T1> ofTuple(Object tuple1){
		return (Tuple1)new Tuples(tuple1);
	}
	public static <T1> Tuple1<T1> of(T1 t1){
		return (Tuple1)new Tuples(Arrays.asList(t1));
	}
	
}
