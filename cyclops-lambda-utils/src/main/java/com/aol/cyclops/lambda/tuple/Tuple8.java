package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

public interface Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> extends Tuple7<T1,T2,T3,T4,T5,T7,T8> {
	
	default T8 v8(){
		return (T8)getCachedValues().get(7);
	}
	default T8 _8(){
		return v8();
	}

	default T8 getT8(){
		return v8();
	}
	default int arity(){
		return 8;
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> ofTuple(Object tuple8){
		return (Tuple8)new Tuples(tuple8);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
																		T6 t6, T7 t7,T8 t8){
		return (Tuple8)new Tuples(Arrays.asList(t1,t2,t3,t4,t5,t6,t7,t8));
	}
}
