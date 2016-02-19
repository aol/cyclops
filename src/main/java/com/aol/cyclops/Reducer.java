package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Reducer<T>  extends Monoid<T>{
	default Stream<T> mapToType(Stream stream){
		return (Stream)stream;
	}
	
	/**
	 * Map a given Stream to required type (via mapToType method), then
	 * reduce using this monoid
	 * 
	 * Example of multiple reduction using multiple Monoids and PowerTuples
	 * <pre>{@code 
	 *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	 *	Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	 *	<PTuple2<Integer,Integer>> result = PowerTuples.tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
	 *										.mapReduce(Stream.of(1,2,3,4)); 
	 *	 
	 *	assertThat(result,equalTo(tuple(10,24)));
	 *  }</pre>
	 * 
	 * @param toReduce Stream to reduce
	 * @return reduced value
	 */
	default T mapReduce(Stream toReduce){
		return reduce(mapToType(toReduce));
	}
	
	public static <T> Reducer<T> fromMonoid(Monoid<T> monoid,Function<?, ? extends T> mapper){
		return of(monoid.zero(),monoid.combiner(),mapper);
	}
	public static <T> Reducer<T> of(T zero, BiFunction<T,T,T> combiner,Function<?,? extends T> mapToType){
		return new Reducer<T>(){
			public T zero(){
				return zero;
			}
			public BiFunction<T,T,T> combiner(){
				return combiner;
			}
			public Stream<T> mapToType(Stream stream){
				return (Stream)stream.map(mapToType);
			}
		};
	}
	public static <T> Reducer<T> of(T zero, Function<T,Function<T,T>> combiner,Function<?,T> mapToType){
		return new Reducer<T>(){
			public T zero(){
				return zero;
			}
			public BiFunction<T,T,T> combiner(){
				return (a,b) -> combiner.apply(a).apply(b);
			}
			public Stream<T> mapToType(Stream stream){
				return (Stream)stream.map(mapToType);
			}
		};
	}
}
