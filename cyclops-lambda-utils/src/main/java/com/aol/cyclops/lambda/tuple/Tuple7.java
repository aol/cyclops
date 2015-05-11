package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;

public interface Tuple7<T1,T2,T3,T4,T5,T6,T7> extends Tuple6<T1,T2,T3,T4,T5,T6> {
	
	default T7 v7(){
		return (T7)getCachedValues().get(6);
	}
	default T7 _7(){
		return v7();
	}

	default T7 getT7(){
		return v7();
	}
	default int arity(){
		return 7;
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
	default Tuple6<T1,T2,T3,T4,T5,T6> tuple6(){
		return this;
	}
	default Tuple7<T7,T6,T5,T4,T3,T2,T1> swap7(){
		return of(v7(),v6(),v5(),v4(),v3(),v2(),v1());
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==7)
			return Optional.of("(%s,%s,%s,%s,%s,%s,%s)");
		return Tuple6.super.asStringFormat(arity);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> ofTuple(Object tuple7){
		return (Tuple7)new Tuples(tuple7,7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
																		T6 t6, T7 t7){
		return (Tuple7)new Tuples(Arrays.asList(t1,t2,t3,t4,t5,t6,t7),7);
	}
}
