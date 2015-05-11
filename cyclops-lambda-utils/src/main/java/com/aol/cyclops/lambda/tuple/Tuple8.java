package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;

public interface Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> extends Tuple7<T1,T2,T3,T4,T5,T6,T7> {
	
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
	default Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple7(){
		return this;
	}
	default Tuple8<T8,T7,T6,T5,T4,T3,T2,T1> swap8(){
		return of(v8(),v7(),v6(),v5(),v4(),v3(),v2(),v1());
	}
	
	default Optional<String> asStringFormat(int arity){
		if(arity()==8)
			return Optional.of("(%s,%s,%s,%s,%s,%s,%s,%s)");
		return Tuple7.super.asStringFormat(arity);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> ofTuple(Object tuple8){
		return (Tuple8)new Tuples(tuple8,8);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
																		T6 t6, T7 t7,T8 t8){
		return (Tuple8)new Tuples(Arrays.asList(t1,t2,t3,t4,t5,t6,t7,t8),8);
	}
}
