package com.aol.cyclops.functions;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.Value;
import lombok.val;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class Memoise {

	/**
	 * Convert a Supplier into one that caches it's result
	 * 
	 * @param s Supplier to memoise
	 * @return Memoised Supplier
	 */
	public static <T> Supplier<T> memoiseSupplier(Supplier<T> s){
		LazyImmutable<T> lazy = LazyImmutable.def();
		return () -> lazy.computeIfAbsent(s);
	}
	/**
	 * Convert a Callable into one that caches it's result
	 * 
	 * @param s Callable to memoise
	 * @return Memoised Callable
	 */
	public static <T> Callable<T> memoiseCallable(Callable<T> s){
		LazyImmutable<T> lazy = LazyImmutable.def();
		return () -> lazy.computeIfAbsent(() -> { 
			try { 
				return s.call();
			}catch(Exception e){
				ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
				return null;
			}
			
		});
	}
	
	/**
	 * Convert a Function into one that caches it's result
	 * 
	 * @param fn Function to memoise
	 * @return Memoised Function
	 */
	public static <T,R> Function<T,R> memoiseFunction(Function<T,R> fn){
		Map<T,R> lazy = new ConcurrentHashMap<>();
		return t -> lazy.computeIfAbsent(t,fn);
	}
	
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> BiFunction<T1, T2, R> memoiseBiFunction(BiFunction<T1, T2, R> fn) {
		val memoise2 = memoiseFunction((Pair<T1,T2> pair) -> fn.apply(pair._1,pair._2));
		return (t1,t2) -> memoise2.apply(new Pair<>(t1,t2));
	}
	/**
	 * Convert a TriFunction into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> TriFunction<T1, T2,T3, R> memoiseTriFunction(TriFunction<T1, T2,T3, R> fn) {
		val memoise2 = memoiseFunction((Triple<T1,T2,T3> triple) -> fn.apply(triple._1,triple._2,triple._3));
		return (t1,t2,t3) -> memoise2.apply(new Triple<>(t1,t2,t3));
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> QuadFunction<T1, T2,T3, T4,R> memoiseQuadFunction(QuadFunction<T1, T2,T3,T4, R> fn) {
		val memoise2 = memoiseFunction((Quad<T1,T2,T3,T4> quad) -> fn.apply(quad._1,quad._2,quad._3,quad._4));
		return (t1,t2,t3,t4) -> memoise2.apply(new Quad<>(t1,t2,t3,t4));
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoisePredicate(Predicate<T> p) {
		Function<T, Boolean> memoised = memoiseFunction((Function<T,Boolean>)t-> p.test(t));
		return (t) -> memoised.apply(t);
	}
	@Value
	private static class Pair<T1,T2>{
		T1 _1;
		T2 _2;
	}
	@Value
	private static class Triple<T1,T2,T3>{
		T1 _1;
		T2 _2;
		T3 _3;
	}
	@Value
	private static class Quad<T1,T2,T3,T4>{
		T1 _1;
		T2 _2;
		T3 _3;
		T4 _4;
	}
}
