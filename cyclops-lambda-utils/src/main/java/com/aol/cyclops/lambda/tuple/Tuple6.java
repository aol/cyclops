package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;

public interface Tuple6<T1,T2,T3,T4,T5,T6> extends Tuple5<T1,T2,T3,T4,T5> {
	
	default T6 v6(){
		return (T6)getCachedValues().get(5);
	}
	default T6 _6(){
		return v6();
	}

	default T6 getT6(){
		return v6();
	}
	default int arity(){
		return 6;
	}
	default Tuple1<T1> tuple1(){
		return this;
	}
	default Tuple2<T1,T2> tuple2(){
		return this;
	}
	default Tuple3<T1,T2,T3> tuple3(){
		return this;
	}
	default Tuple4<T1,T2,T3,T4> tuple4(){
		return this;
	}
	default Tuple5<T1,T2,T3,T4,T5> tuple5(){
		return this;
	}
	default Tuple6<T6,T5,T4,T3,T2,T1> swap6(){
		return of(v6(),v5(),v4(),v3(),v2(),v1());
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==6)
			return Optional.of("(%s,%s,%s,%s,%s,%s)");
		return Tuple5.super.asStringFormat(arity);
	}
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> ofTuple(Object tuple6){
		return (Tuple6)new Tuples(tuple6,6);
	}
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5, T6 t6){
		return (Tuple6)new Tuples(Arrays.asList(t1,t2,t3,t4,t5,t6),6);
	}
}
