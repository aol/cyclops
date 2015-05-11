package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;

public interface Tuple3<T1,T2,T3> extends Tuple2<T1,T2> {
	
	default T3 v3(){
		return (T3)getCachedValues().get(2);
	}
	default T3 _3(){
		return v3();
	}

	default T3 getT3(){
		return v3();
	}
	default int arity(){
		return 3;
	}
	
	default Tuple1<T1> tuple1(){
		return this;
	}
	default Tuple2<T1,T2> tuple2(){
		return this;
	}
	default Tuple3<T3,T2,T1> swap3(){
		return of(v3(),v2(),v1());
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==3)
			return Optional.of("(%s,%s,%s)");
		return Tuple2.super.asStringFormat(arity);
	}
	
	public static <T1,T2,T3> Tuple3<T1,T2,T3> ofTuple(Object tuple2){
		return (Tuple3)new Tuples(tuple2,3);
	}
	public static <T1,T2,T3> Tuple3<T1,T2,T3> of(T1 t1, T2 t2,T3 t3){
		return (Tuple3)new Tuples(Arrays.asList(t1,t2,t3),3);
	}
}
