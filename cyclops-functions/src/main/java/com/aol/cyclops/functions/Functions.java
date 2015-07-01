package com.aol.cyclops.functions;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.lambda.monads.AnyM;

public class Functions extends Uncurry {
	/**
	 * Convert a Supplier into one that caches it's result
	 * 
	 * @param s Supplier to memoise
	 * @return Memoised Supplier
	 */
	public static <T> Supplier<T> memoiseSupplier(Supplier<T> s){
		return Memoise.memoiseSupplier(s);
	}
	/**
	 * Convert a Callable into one that caches it's result
	 * 
	 * @param s Callable to memoise
	 * @return Memoised Callable
	 */
	public static <T> Callable<T> memoiseCallable(Callable<T> s){
		return Memoise.memoiseCallable(s);
	}
	
	/**
	 * Convert a Function into one that caches it's result
	 * 
	 * @param fn Function to memoise
	 * @return Memoised Function
	 */
	public static <T,R> Function<T,R> memoiseFunction(Function<T,R> fn){
		return Memoise.memoiseFunction(fn);
	}
	
	/**
	 * Convert a BiFunction into one that caches it's result
	 * 
	 * @param fn BiFunction to memoise
	 * @return Memoised BiFunction
	 */
	public static <T1,T2 , R> BiFunction<T1, T2, R> memoiseBiFunction(BiFunction<T1, T2, R> fn) {
		return Memoise.memoiseBiFunction(fn);
	}
	/**
	 * Convert a TriFunction into one that caches it's result
	 * 
	 * @param fn TriFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3, R> TriFunction<T1, T2,T3, R> memoiseTriFunction(TriFunction<T1, T2,T3, R> fn) {
		return Memoise.memoiseTriFunction(fn);
	}
	/**
	 * Convert a QuadFunction into one that caches it's result
	 * 
	 * @param fn QuadFunction to memoise
	 * @return Memoised TriFunction
	 */
	public static <T1,T2,T3,T4, R> QuadFunction<T1, T2,T3, T4,R> memoiseQuadFunction(QuadFunction<T1, T2,T3,T4, R> fn) {
		return Memoise.memoiseQuadFunction(fn);
	}
	/**
	 * Convert a Predicate into one that caches it's result
	 * 
	 * @param p Predicate to memoise
	 * @return Memoised Predicate
	 */
	public static <T> Predicate<T> memoisePredicate(Predicate<T> p) {
		return Memoise.memoisePredicate(p);
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
		return LiftMFunctions.liftM(fn);
	}
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * AnyM view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * Example of lifting an existing method to add error handling (Try Monad) and iteration (Stream monad)
	 * <pre>
	 * {@code
	 	//using Lombok val to simplify verbose generics
	    val divide = LiftMFunctions.liftM2(this::divide);
		
		AnyM<Integer> result = divide.apply(anyM(Try.of(2, ArithmeticException.class)), anyM(Stream.of(10,1,2,3)));
		
		assertThat(result.<Try<List<Integer>,ArithmeticException>>unwrapMonad().get(),equalTo(Arrays.asList(0, 2, 1, 0)));
	  
	  	private Integer divide(Integer a, Integer b){
			return a/b;
		}
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	public static <U1,U2,R> BiFunction<AnyM<U1>,AnyM<U2>,AnyM<R>> liftM2(BiFunction<U1,U2,R> fn){
		return LiftMFunctions.liftM2(fn);
	}
	
	/**
	 * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
	 * 
	 * <pre>
	 * {@code
	 * TriFunction<AnyM<Double>,AnyM<Entity>,AnyM<String>,AnyM<Integer>> fn = liftM3(this::myMethod);
	 *    
	 * }
	 * </pre>
	 * 
	 * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
	 * 
	 * @param fn Function to lift
	 * @return Lifted function
	 */
	public static <U1,U2,U3,R> TriFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<R>> liftM3(TriFunction<U1,U2,U3,R> fn){
		return LiftMFunctions.liftM3(fn);
	}
	
	/**
	 * Lift a QuadFunction into Monadic form.
	 * 
	 * @param fn Quad funciton to lift
	 * @return Lifted Quad function
	 */
	public static <U1,U2,U3,U4,R> QuadFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<R>> liftM4(QuadFunction<U1,U2,U3,U4,R> fn){
		
		return LiftMFunctions.liftM4(fn);
	}
	
	/**
	 * Lift a QuintFunction (5 parameters) into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted Function
	 */
	public static <U1,U2,U3,U4,U5,R> QuintFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<U5>,AnyM<R>> liftM5(QuintFunction<U1,U2,U3,U4,U5,R> fn){
		
		return LiftMFunctions.liftM5(fn);
	}
	
	/**
	 * Lift a Curried Function {@code (2 levels a->b->fn.apply(a,b) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,R> Function<AnyM<U1>,Function<AnyM<U2>,AnyM<R>>> liftM2(Function<U1,Function<U2,R>> fn){
		return LiftMFunctions.liftM2(fn);

	}
	/**
	 * Lift a Curried Function {@code (3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,AnyM<R>>>> liftM3(Function<U1,Function<U2,Function<U3,R>>> fn){
		return LiftMFunctions.liftM3(fn);
	}
	
	/**
	 * Lift a Curried Function {@code (4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,U4,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,AnyM<R>>>>> liftM4(Function<U1,Function<U2,Function<U3,Function<U4,R>>>> fn){
		
		return LiftMFunctions.liftM4(fn);
	}
	/**
	 * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,U4,U5,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,Function<AnyM<U5>,AnyM<R>>>>>> liftM5(Function<U1,Function<U2,Function<U3,Function<U4,Function<U5,R>>>>> fn){
		
		return LiftMFunctions.liftM5(fn);
	}

}
