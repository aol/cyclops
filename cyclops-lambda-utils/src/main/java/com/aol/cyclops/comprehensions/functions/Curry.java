package com.aol.cyclops.comprehensions.functions;

import java.util.function.BiFunction;
import java.util.function.Function;

//#http://java.dzone.com/articles/whats-wrong-java-8-currying-vs
public class Curry {

	
	public static <T1,T2,R> Function<T1,Function<T2,R>> curry2(BiFunction<T1,T2,R> biFunc){
		return  t1 -> t2 -> biFunc.apply(t1,t2);
	}
	
	public static <T1,T2,T3,R> Function<T1,Function<T2,Function<T3,R>>> curry3(TriFunction<T1,T2,T3,R> triFunc){
		return   t1-> t2 -> t3 -> triFunc.apply(t1,t2,t3);
	}
	public static <T1,T2,T3,T4,R> Function<T1,Function<T2,Function<T3,Function<T4,R>>>> curry4(QuadFunction<T1,T2,T3,T4,R> quadFunc){
		return   t1-> t2-> t3 -> t4 -> quadFunc.apply(t1,t2,t3,t4);
	}
	public static <T1,T2,T3,T4,T5,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> curry5(QuintFunction<T1,T2,T3,T4,T5,R> pentFunc){
		return   t1 -> t2-> t3-> t4 -> t5 -> pentFunc.apply(t1,t2,t3,t4,t5);
	}
	public static <T1,T2,T3,T4,T5,T6,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>>  curry6(HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
		return   t1 -> t2 -> t3-> t4-> t5 -> t6 -> hexFunc.apply(t1,t2,t3,t4,t5, t6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>>  curry7(HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
		return   t1->t2 -> t3 -> t4-> t5-> t6 -> t7 -> heptFunc.apply(t1,t2,t3,t4,t5, t6,t7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  curry8(OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
		return   t1->t2 -> t3 -> t4-> t5-> t6 -> t7 -> t8-> octFunc.apply(t1,t2,t3,t4,t5, t6,t7,t8);
	}
	
}
