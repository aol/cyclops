package com.aol.cyclops.matcher.builders;

import lombok.Value;

@Value
class Tuple2<T1,T2> {
	public final T1 v1;
	public final T2 v2;
	
	static <T1,T2> Tuple2<T1,T2> tuple(T1 t1, T2 t2){
		return new Tuple2<>(t1,t2);
	}
}
