package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

import lombok.AllArgsConstructor;

public class Tuples {
	
	@AllArgsConstructor
	public static class ConvertStep<T extends CachedValues>{
		private final T c;
		public <X> X to(Class<X> to){
			return (X)c.to(to);
		}
	}
	public static <T extends CachedValues> ConvertStep<T> convert(T c){
		return new ConvertStep(c);
		}
	/**
	public static <X,T extends CachedValues> X convert(T c,Class<X> to){
		return (X)c.to(to);
	}**/
	public static <T1> Tuple1<T1> tuple(T1 t1){
		return (Tuple1)new TupleImpl(Arrays.asList(t1),1);
	}
	
	public static <T1,T2> Tuple2<T1,T2> tuple(T1 t1, T2 t2){
		return (Tuple2)new TupleImpl(Arrays.asList(t1,t2),2);
	}
	
	public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(T1 t1, T2 t2,T3 t3){
		return (Tuple3)new TupleImpl(Arrays.asList(t1,t2,t3),3);
	}
	
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(T1 t1, T2 t2,T3 t3,T4 t4){
		return (Tuple4)new TupleImpl(Arrays.asList(t1,t2,t3,t4),4);
	}
	
	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5){
		return (Tuple5)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5),5);
	}
	
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5, T6 t6){
		return (Tuple6)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6),6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
			T6 t6, T7 t7){
		return (Tuple7)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7),7);
	}
	
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
			T6 t6, T7 t7,T8 t8){
		return (Tuple8)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7,t8),8);
	}
}
