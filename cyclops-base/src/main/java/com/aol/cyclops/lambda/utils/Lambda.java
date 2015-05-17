package com.aol.cyclops.lambda.utils;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Lambda {
	public static <T1,R> Function<T1,R> λ1(Function<T1,R> func){
		return func;
	}
	public static <T1,T2,R> Function<T1,Function<T2,R>> λ2(Function<T1,Function<T2,R>> biFunc){
		return biFunc;
	}
	
	public static <T1,T2,T3,R> Function<T1,Function<T2,Function<T3,R>>> λ3(Function<T1,Function<T2,Function<T3,R>>> triFunc){
		return   triFunc;
	}
	public static <T1,T2,T3,T4,R> Function<T1,Function<T2,Function<T3,Function<T4,R>>>> λ4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> quadFunc){
		return   quadFunc;
	}
	public static <T1,T2,T3,T4,T5,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> λ5(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> pentFunc){
		return   pentFunc;
	}
	public static <T1,T2,T3,T4,T5,T6,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>>  λ6(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> hexFunc){
		return   hexFunc;
	}
	public static <T1,T2,T3,T4,T5,T6,T7,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>>  λ7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> heptFunc){
		return   heptFunc;
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  λ8(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>> octFunc){
		return  octFunc;
	}
}
