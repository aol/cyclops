package com.aol.cyclops.lambda.api;


import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;





/**
 * Practically the method signature to reduce matches the Monoid interface
 * Monoids could regrarded as immutable equivalents to JDK Collectors for Immutable Reduction
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Monoid<T> {

	T zero();
	
	BiFunction<T,T,T> combiner();
	
	default BinaryOperator<T> reducer(){
		return (a,b) -> combiner().apply(a,b);
	}
	
	default Stream<T> mapToType(Stream stream){
		return (Stream)stream;
	}
	
	/**
	 * Map a given Stream to required type (via mapToType method), then
	 * reduce using this monoid
	 * 
	 * Example of multiple reduction using multiple Monoids and PowerTuples
	 * {@code 
	 *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	 *	Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	 *	<PTuple2<Integer,Integer>> result = PowerTuples.tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
	 *										.mapReduce(Stream.of(1,2,3,4)); 
	 *	 
	 *	assertThat(result,equalTo(tuple(10,24)));
	 *  }
	 * 
	 * @param toReduce Stream to reduce
	 * @return reduced value
	 */
	default T mapReduce(Stream toReduce){
		return reduce(mapToType(toReduce));
	}
	default T reduce(Stream<T> toReduce){
		return mapToType(toReduce).reduce(zero(),reducer());
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
