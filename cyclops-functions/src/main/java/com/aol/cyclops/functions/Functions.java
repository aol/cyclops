package com.aol.cyclops.functions;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.functions.caching.Memoize;
import com.aol.cyclops.functions.currying.Uncurry;

public class Functions extends Uncurry {
	/**
	 * Convert a Supplier into one that caches it's result
	 * 
	 * @param s Supplier to memoise
	 * @return Memoised Supplier
	 */
	public static <T> Supplier<T> memoiseSupplier(Supplier<T> s){
		return Memoize.memoizeSupplier(s);
	}
	/**
	 * Convert a Callable into one that caches it's result
	 * 
	 * @param s Callable to memoise
	 * @return Memoised Callable
	 */
	public static <T> Callable<T> memoiseCallable(Callable<T> s){
		return Memoize.memoizeCallable(s);
	}
	
	/**
	 * Convert a Function into one that caches it's result
	 * 
	 * @param fn Function to memoise
	 * @return Memoised Function
	 */
	public static <T,R> Function<T,R> memoiseFunction(Function<T,R> fn){
		return Memoize.memoizeFunction(fn);
	}
	
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> BiFunction<T1, T2, R> memoiseBiFunction(BiFunction<T1, T2, R> fn) {
		return Memoize.memoizeBiFunction(fn);
	}
	/**
	 * Convert a TriFunction into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> TriFunction<T1, T2,T3, R> memoiseTriFunction(TriFunction<T1, T2,T3, R> fn) {
		return Memoize.memoizeTriFunction(fn);
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> QuadFunction<T1, T2,T3, T4,R> memoiseQuadFunction(QuadFunction<T1, T2,T3,T4, R> fn) {
		return Memoize.memoizeQuadFunction(fn);
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoisePredicate(Predicate<T> p) {
		return Memoize.memoizePredicate(p);
	}
	

}
