package com.aol.cyclops.functions.caching;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.Value;
import lombok.val;

import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.TriFunction;

public class Memoize {

	/**
	 * Convert a Supplier into one that caches it's result
	 * 
	 * @param s Supplier to memoise
	 * @return Memoised Supplier
	 */
	public static <T> Supplier<T> memoizeSupplier(Supplier<T> s){
		Map<Object,T> lazy = new ConcurrentHashMap<>();
		return () -> lazy.computeIfAbsent("k",a->s.get());
	}
	/**
	 * Convert a Supplier into one that caches it's result
	 * 
	 * @param s Supplier to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised Supplier
	 */
	public static <T> Supplier<T> memoizeSupplier(Supplier<T> s,Cachable<T> cache){
		
		return () -> cache.computeIfAbsent("k",a->s.get());
	}
	
	/**
	 * Convert a Callable into one that caches it's result
	 * 
	 * @param s Callable to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised Callable
	 */
	public static <T> Callable<T> memoizeCallable(Callable<T> s,Cachable<T> cache){
	
		return () -> cache.computeIfAbsent("k",a -> { 
			try { 
				return s.call();
			}catch(Exception e){
				throwSoftenedException(e);
				return null;
			}
			
		});
	}
	/**
	 * Convert a Callable into one that caches it's result
	 * 
	 * @param s Callable to memoise
	 * @return Memoised Callable
	 */
	public static <T> Callable<T> memoizeCallable(Callable<T> s){
		Map<Object,T> lazy = new ConcurrentHashMap<>();
		return () -> lazy.computeIfAbsent("k",a -> { 
			try { 
				return s.call();
			}catch(Exception e){
				throwSoftenedException(e);
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
	public static <T,R> Function<T,R> memoizeFunction(Function<T,R> fn){
		Map<T,R> lazy = new ConcurrentHashMap<>();
		return t -> lazy.computeIfAbsent(t,fn);
	}
	/**
	 * Convert a Function into one that caches it's result
	 * 
	 * @param fn Function to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised Function
	 */
	public static <T,R> Function<T,R> memoizeFunction(Function<T,R> fn,Cachable<R> cache){
		return t -> (R)cache.computeIfAbsent(t,(Function)fn);
	}
	
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> BiFunction<T1, T2, R> memoizeBiFunction(BiFunction<T1, T2, R> fn) {
		val memoise2 = memoizeFunction((Pair<T1,T2> pair) -> fn.apply(pair._1,pair._2));
		return (t1,t2) -> memoise2.apply(new Pair<>(t1,t2));
	}
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> BiFunction<T1, T2, R> memoizeBiFunction(BiFunction<T1, T2, R> fn,Cachable<R> cache) {
		val memoise2 = memoizeFunction((Pair<T1,T2> pair) -> fn.apply(pair._1,pair._2),cache);
		return (t1,t2) -> memoise2.apply(new Pair<>(t1,t2));
	}
	/**
	 * Convert a TriFunction into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> TriFunction<T1, T2,T3, R> memoizeTriFunction(TriFunction<T1, T2,T3, R> fn) {
		val memoise2 = memoizeFunction((Triple<T1,T2,T3> triple) -> fn.apply(triple._1,triple._2,triple._3));
		return (t1,t2,t3) -> memoise2.apply(new Triple<>(t1,t2,t3));
	}
	/**
	 * Convert a TriFunction into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> TriFunction<T1, T2,T3, R> memoizeTriFunction(TriFunction<T1, T2,T3, R> fn,Cachable<R> cache) {
		val memoise2 = memoizeFunction((Triple<T1,T2,T3> triple) -> fn.apply(triple._1,triple._2,triple._3),cache);
		return (t1,t2,t3) -> memoise2.apply(new Triple<>(t1,t2,t3));
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> QuadFunction<T1, T2,T3, T4,R> memoizeQuadFunction(QuadFunction<T1, T2,T3,T4, R> fn) {
		val memoise2 = memoizeFunction((Quad<T1,T2,T3,T4> quad) -> fn.apply(quad._1,quad._2,quad._3,quad._4));
		return (t1,t2,t3,t4) -> memoise2.apply(new Quad<>(t1,t2,t3,t4));
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> QuadFunction<T1, T2,T3, T4,R> memoizeQuadFunction(QuadFunction<T1, T2,T3,T4, R> fn,Cachable<R> cache) {
		val memoise2 = memoizeFunction((Quad<T1,T2,T3,T4> quad) -> fn.apply(quad._1,quad._2,quad._3,quad._4),cache);
		return (t1,t2,t3,t4) -> memoise2.apply(new Quad<>(t1,t2,t3,t4));
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoizePredicate(Predicate<T> p) {
		Function<T, Boolean> memoised = memoizeFunction((Function<T,Boolean>)t-> p.test(t));
		return (t) -> memoised.apply(t);
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @param cache Cachable to store the results
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoizePredicate(Predicate<T> p,Cachable<Boolean> cache) {
		Function<T, Boolean> memoised = memoizeFunction((Function<T,Boolean>)t-> p.test(t),cache);
		return (t) -> memoised.apply(t);
	}
	@Value
	static class Pair<T1,T2>{
		T1 _1;
		T2 _2;
	}
	@Value
	static class Triple<T1,T2,T3>{
		T1 _1;
		T2 _2;
		T3 _3;
	}
	@Value
	static class Quad<T1,T2,T3,T4>{
		T1 _1;
		T2 _2;
		T3 _3;
		T4 _4;
	}
	private static void throwSoftenedException(final Throwable e) {
		new Thrower<RuntimeException>().uncheck(e);
	}
	static class Thrower<T extends Throwable> {
		@SuppressWarnings("unchecked")
			private void uncheck(Throwable throwable) throws T {
			 	throw (T) throwable;
			 }
	}
}
