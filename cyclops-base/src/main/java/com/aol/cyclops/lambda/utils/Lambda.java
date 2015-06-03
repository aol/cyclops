package com.aol.cyclops.lambda.utils;

import java.util.function.Function;

/**
 * Lambda type inferencing helper / curried function creation helper
 * 
 * @author johnmcclean
 *
 */
public class Lambda {
	
	/**
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ1((Integer i)->"hello")
	 * }
	 * @param func
	 * @return supplied function
	 */
	public static <T1,R> Function<T1,R> λ1(Function<T1,R> func){
		return func;
	}
	/**
	 * Create a curried function with arity of 2
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ3((Integer a)-> (Integer b)-> a+b+)
	 * }
	 * @param biFunc
	 * @return supplied function
	 */
	public static <T1,T2,R> Function<T1,Function<T2,R>> λ2(Function<T1,Function<T2,R>> biFunc){
		return biFunc;
	}
	/**
	 * Create a curried function with arity of 3
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ3((Integer a)-> (Integer b)->(Integer c) -> a+b+c)
	 * }
	 * @param triFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,R> Function<T1,Function<T2,Function<T3,R>>> λ3(Function<T1,Function<T2,Function<T3,R>>> triFunc){
		return   triFunc;
	}
	/**
	 * Create a curried function with arity of 4
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> a+b+c+d)
	 * }
	 * @param quadFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,R> Function<T1,Function<T2,Function<T3,Function<T4,R>>>> λ4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> quadFunc){
		return   quadFunc;
	}
	/**
	 * Create a curried function with arity of 5
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> a+b+c+d+e)
	 * }
	 * @param pentFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> λ5(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> pentFunc){
		return   pentFunc;
	}
	/**
	 * Create a curried function with arity of 6
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)-> a+b+c+d+e+f)
	 * }
	 * @param hexFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,T6,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>>  λ6(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> hexFunc){
		return   hexFunc;
	}
	/**
	 * Create a curried function with arity of 7
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)->(Integer g) -> a+b+c+d+e+f+g)
	 * }
	 * @param heptFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,T6,T7,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>>  λ7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> heptFunc){
		return   heptFunc;
	}
	/**
	 * Create a curried function with arity of 8
	 * 
	 * e.g. with Lombok val 
	 * 
	 * {@code
	 * 		val fn  = λ4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)->(Integer g) -> (Integer h) ->a+b+c+d+e+f+g+h)
	 * }
	 * @param octFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,T6,T7,T8,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  λ8(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>> octFunc){
		return  octFunc;
	}
}
