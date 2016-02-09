package com.aol.cyclops.sequence;


import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.Semigroup;





/**
 * An interoperability trait for Monoids
 * 
 * Also inteded for use with Java 8 Streams (reduce method)
 * 
 * Practically the method signature to reduce matches the Monoid interface
 * Monoids could regrarded as immutable equivalents to JDK Collectors for Immutable Reduction
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Monoid<T> extends Semigroup<T> {

	/**
	 * An element that when provided as a parameter to the combiner with another value, results
	 * in the other value being returned
	 * e.g.
	 * <pre>
	 *  0  + 1  = 1
	 *  
	 *  0 is zero()
	 *  
	 *  1 * 2 = 2
	 *  
	 *   1 is zero()
	 *   
	 *   "" + "hello" = "hello"
	 *   
	 *  "" is zero()
	 *  </pre>
	 * @return
	 */
	T zero();
	
	
	
	
	default T reduce(Stream<T> toReduce){
		return toReduce.reduce(zero(),reducer());
	}
	
	
	public static <T> Monoid<T> of(T zero, Function<T,Function<T,T>> combiner){
		return new Monoid<T>(){
			public T zero(){
				return zero;
			}
			public BiFunction<T,T,T> combiner(){
				return (a,b) -> combiner.apply(a).apply(b);
			}
		};
	}
	public static <T> Monoid<T> of(T zero, BiFunction<T,T,T> combiner){
		return new Monoid<T>(){
			public T zero(){
				return zero;
			}
			public BiFunction<T,T,T> combiner(){
				return combiner;
			}
		};
	}
}
