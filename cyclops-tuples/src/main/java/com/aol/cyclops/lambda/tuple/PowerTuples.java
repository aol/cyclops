package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;

import lombok.AllArgsConstructor;

/**
 * Highly dynamic / powerful Tuples
 * @author johnmcclean
 *
 *  {@code
 *    method1().call(this::method2)
 *  
 *  //results in 10hello
 *  
 *  public PTuple2<Integer,String> method1(){
		return PowerTuples.tuple(10,"hello");
	}
	
	public String method2(Integer number, String value){
		return "" + number + value;
	}
 *  }
 *
 */
public class PowerTuples {
	
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
	
	public static <T1> PTuple1<T1> tuple(T1 t1){
		return (PTuple1)new TupleImpl(Arrays.asList(t1),1);
	}
	
	public static <T1,T2> PTuple2<T1,T2> tuple(T1 t1, T2 t2){
		return (PTuple2)new TupleImpl(Arrays.asList(t1,t2),2);
	}
	
	public static <T1,T2,T3> PTuple3<T1,T2,T3> tuple(T1 t1, T2 t2,T3 t3){
		return (PTuple3)new TupleImpl(Arrays.asList(t1,t2,t3),3);
	}
	
	public static <T1,T2,T3,T4> PTuple4<T1,T2,T3,T4> tuple(T1 t1, T2 t2,T3 t3,T4 t4){
		return (PTuple4)new TupleImpl(Arrays.asList(t1,t2,t3,t4),4);
	}
	
	public static <T1,T2,T3,T4,T5> PTuple5<T1,T2,T3,T4,T5> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5){
		return (PTuple5)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5),5);
	}
	
	public static <T1,T2,T3,T4,T5,T6> PTuple6<T1,T2,T3,T4,T5,T6> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5, T6 t6){
		return (PTuple6)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6),6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> PTuple7<T1,T2,T3,T4,T5,T6,T7> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
			T6 t6, T7 t7){
		return (PTuple7)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7),7);
	}
	
	public static <T1,T2,T3,T4,T5,T6,T7,T8> PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
			T6 t6, T7 t7,T8 t8){
		return (PTuple8)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7,t8),8);
	}
}
