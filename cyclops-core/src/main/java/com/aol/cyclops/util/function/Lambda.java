package com.aol.cyclops.util.function;


import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Lambda type inferencing helper / curried function creation helper
 * 
 * @author johnmcclean
 *
 */
public class Lambda {
	/**
	 * E.g. to use a supplier to embed additional code inisde a terenury operator
	 * 
	 * <pre>
	 * {@code 
	 * return pos >= values.length ? tuple(true, split) : Lambda.s(() -> {
				action.accept(values[pos++]);
				return tuple(true, this);
			}).get();
	 * 
	 * }
	 * </pre>
	 * 
	 * @param supplier Lambda / method to assign type of Supplier to  
	 * @return Supplier
	 */
	public static <T> Supplier<T> s(Supplier<T> supplier){
		return supplier;
	}
	/**
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l1((Integer i)->"hello")
	 * }</pre>
	 * @param func
	 * @return supplied function
	 */
	public static <T1,R> Function<T1,R> l1(Function<T1,R> func){
		return func;
	}
	/**
	 * Create a curried function with arity of 2
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l3((Integer a)-> (Integer b)-> a+b+)
	 * }</pre>
	 * @param biFunc
	 * @return supplied function
	 */
	public static <T1,T2,R> Function<T1,Function<T2,R>> l2(Function<T1,Function<T2,R>> biFunc){
		return biFunc;
	}
	/**
	 * Create a curried function with arity of 3
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l3((Integer a)-> (Integer b)->(Integer c) -> a+b+c)
	 * }</pre>
	 * @param triFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,R> Function<T1,Function<T2,Function<T3,R>>> l3(Function<T1,Function<T2,Function<T3,R>>> triFunc){
		return   triFunc;
	}
	/**
	 * Create a curried function with arity of 4
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> a+b+c+d)
	 * }</pre>
	 * @param quadFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,R> Function<T1,Function<T2,Function<T3,Function<T4,R>>>> l4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> quadFunc){
		return   quadFunc;
	}
	/**
	 * Create a curried function with arity of 5
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> a+b+c+d+e)
	 * }</pre>
	 * @param pentFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> l5(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> pentFunc){
		return   pentFunc;
	}
	/**
	 * Create a curried function with arity of 6
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)-> a+b+c+d+e+f)
	 * }</pre>
	 * @param hexFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,T6,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>>  l6(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> hexFunc){
		return   hexFunc;
	}
	/**
	 * Create a curried function with arity of 7
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)->(Integer g) -> a+b+c+d+e+f+g)
	 * }</pre>
	 * @param heptFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,T6,T7,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>>  l7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> heptFunc){
		return   heptFunc;
	}
	/**
	 * Create a curried function with arity of 8
	 * 
	 * e.g. with Lombok val 
	 * 
	 * <pre>{@code
	 * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)->(Integer g) -> (Integer h) ->a+b+c+d+e+f+g+h)
	 * }</pre>
	 * @param octFunc
	 * @return supplied function
	 */
	public static <T1,T2,T3,T4,T5,T6,T7,T8,R> Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  l8(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>> octFunc){
		return  octFunc;
	}
}
