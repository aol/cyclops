package com.aol.cyclops.javaslang.caching;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import javaslang.Function0;
import javaslang.Function1;
import javaslang.Function2;
import javaslang.Function3;
import javaslang.Function4;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import lombok.val;

import com.aol.cyclops.functions.caching.Cacheable;

public class Memoize {

	/**
	 * Convert a Function0 into one that caches it's result
	 * 
	 * @param s Function0 to memoise
	 * @return Memoised Function0
	 */
	public static <T> Function0<T> memoizeFunction0(Function0<T> s){
		Map<Object,T> lazy = new ConcurrentHashMap<>();
		return () -> lazy.computeIfAbsent("k",a->s.get());
	}
	/**
	 * Convert a Function0 into one that caches it's result
	 * 
	 * @param s Function0 to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised Function0
	 */
	public static <T> Function0<T> memoizeFunction0(Function0<T> s,Cacheable<T> cache){
		
		return () -> cache.soften().computeIfAbsent("k",a->s.get());
	}
	
	/**
	 * Convert a Callable into one that caches it's result
	 * 
	 * @param s Callable to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised Callable
	 */
	public static <T> Callable<T> memoizeCallable(Callable<T> s,Cacheable<T> cache){
	
		return () -> cache.soften().computeIfAbsent("k",a -> { 
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
	public static <T,R> Function<T,R> memoizeFunction(Function1<T,R> fn){
		Map<T,R> lazy = new ConcurrentHashMap<>();
		return t -> lazy.computeIfAbsent(t,fn);
	}
	/**
	 * Convert a Function into one that caches it's result
	 * 
	 * @param fn Function to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised Function
	 */
	public static <T,R> Function<T,R> memoizeFunction(Function1<T,R> fn,Cacheable<R> cache){
		return t -> (R)cache.soften().computeIfAbsent(t,(Function)fn);
	}
	
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> Function2<T1, T2, R> memoizeBiFunction(Function2<T1, T2, R> fn) {
		val memoise2 = memoizeFunction((Tuple2<T1,T2> pair) -> fn.apply(pair._1,pair._2));
		return (t1,t2) -> memoise2.apply(new Tuple2<>(t1,t2));
	}
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> Function2<T1, T2, R> memoizeBiFunction(Function2<T1, T2, R> fn,Cacheable<R> cache) {
		val memoise2 = memoizeFunction((Tuple2<T1,T2> pair) -> fn.apply(pair._1,pair._2),cache);
		return (t1,t2) -> memoise2.apply(new Tuple2<>(t1,t2));
	}
	/**
	 * Convert a Function3 into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> Function3<T1, T2,T3, R> memoizeTriFunction(Function3<T1, T2,T3, R> fn) {
		val memoise2 = memoizeFunction((Tuple3<T1,T2,T3> triple) -> fn.apply(triple._1,triple._2,triple._3));
		return (t1,t2,t3) -> memoise2.apply(new Tuple3<>(t1,t2,t3));
	}
	/**
	 * Convert a Function3 into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> Function3<T1, T2,T3, R> memoizeTriFunction(Function3<T1, T2,T3, R> fn,Cacheable<R> cache) {
		val memoise2 = memoizeFunction((Tuple3<T1,T2,T3> triple) -> fn.apply(triple._1,triple._2,triple._3),cache);
		return (t1,t2,t3) -> memoise2.apply(new Tuple3<>(t1,t2,t3));
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> Function4<T1, T2,T3, T4,R> memoizeQuadFunction(Function4<T1, T2,T3,T4, R> fn) {
		val memoise2 = memoizeFunction((Tuple4<T1,T2,T3,T4> quad) -> fn.apply(quad._1,quad._2,quad._3,quad._4));
		return (t1,t2,t3,t4) -> memoise2.apply(new Tuple4<>(t1,t2,t3,t4));
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> Function4<T1, T2,T3, T4,R> memoizeQuadFunction(Function4<T1, T2,T3,T4, R> fn,Cacheable<R> cache) {
		val memoise2 = memoizeFunction((Tuple4<T1,T2,T3,T4> quad) -> fn.apply(quad._1,quad._2,quad._3,quad._4),cache);
		return (t1,t2,t3,t4) -> memoise2.apply(new Tuple4<>(t1,t2,t3,t4));
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoizePredicate(Predicate<T> p) {
		Function<T, Boolean> memoised = memoizeFunction((Function1<T,Boolean>)t-> p.test(t));
		return (t) -> memoised.apply(t);
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @param cache Cacheable to store the results
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoizePredicate(Predicate<T> p,Cacheable<Boolean> cache) {
		Function<T, Boolean> memoised = memoizeFunction((Function1<T,Boolean>)t-> p.test(t),cache);
		return (t) -> memoised.apply(t);
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
