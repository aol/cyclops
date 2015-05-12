package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import lombok.val;

import com.aol.cyclops.comprehensions.functions.HexFunction;
import com.aol.cyclops.lambda.utils.ImmutableClosedValue;

public interface Tuple6<T1,T2,T3,T4,T5,T6> extends Tuple5<T1,T2,T3,T4,T5> {
	
	default T6 v6(){
		if(arity()<6)
			throw new ClassCastException("Attempt to upscale to " + Tuple6.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
		return (T6)getCachedValues().get(5);
	}
	default T6 _6(){
		return v6();
	}

	default T6 getT6(){
		return v6();
	}
	default int arity(){
		return 6;
	}
	default <R> R apply6(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> fn){
		return fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6());
	}
	default <R> R call(HexFunction<T1,T2,T3,T4,T5,T6,R> fn){
		return fn.apply(v1(),v2(),v3(),v4(),v5(),v6());
	}
	default <R> CompletableFuture<R>  callAsync(HexFunction<T1,T2,T3,T4,T5,T6,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6()));
	}
	default <R> CompletableFuture<R> applyAsync6(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> fn){
		return CompletableFuture.completedFuture(v6())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()));
	}
	default <R> CompletableFuture<R>  callAsync(HexFunction<T1,T2,T3,T4,T5,T6,R> fn, Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6()),e);
	}
	default <R> CompletableFuture<R> applyAsync6(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> fn, Executor e){
		return CompletableFuture.completedFuture(v6())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()),e);
	}
	
	default Tuple1<T1> tuple1(){
		return this;
	}
	default Tuple2<T1,T2> tuple2(){
		return this;
	}
	default Tuple3<T1,T2,T3> tuple3(){
		return this;
	}
	default Tuple4<T1,T2,T3,T4> tuple4(){
		return this;
	}
	default Tuple5<T1,T2,T3,T4,T5> tuple5(){
		return this;
	}
	default Tuple6<T6,T5,T4,T3,T2,T1> swap6(){
		return of(v6(),v5(),v4(),v3(),v2(),v1());
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==6)
			return Optional.of("(%s,%s,%s,%s,%s,%s)");
		return Tuple5.super.asStringFormat(arity);
	}
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> ofTuple(Object tuple6){
		return (Tuple6)new TupleImpl(tuple6,6);
	}
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5, T6 t6){
		return (Tuple6)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6),6);
	}
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple6
	 */
	default <T> Tuple6<T,T2,T3,T4,T5,T6> map1(Function<T1,T> fn){
		return Tuples.tuple(fn.apply(v1()),v2(),v3(),v4(),v5(),v6());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple6<T,T2,T3,T4,T5,T6> lazyMap1(Function<T1,T> fn){
		val tuple = this;
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new Tuple6<T,T2,T3,T4,T5,T6>(){
			public T v1(){
				return value.getOrSet(()->fn.apply(tuple.v1())); 
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	/**
	 * Lazily Map 2nd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple6<T1,T,T3,T4,T5,T6> lazyMap2(Function<T2,T> fn){
		val tuple = this;
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new Tuple6<T1,T,T3,T4,T5,T6>(){
			
			public T v2(){
				return value.getOrSet(()->fn.apply(tuple.v2())); 
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	
	/** Map the second element in this Tuple
	 * @param fn mapper function
	 * @return new Tuple3
	 */
	default <T> Tuple6<T1,T,T3,T4,T5,T6> map2(Function<T2,T> fn){
		return of(v1(),fn.apply(v2()),v3(),v4(),v5(),v6());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple6<T1,T2,T,T4,T5,T6> lazyMap3(Function<T3,T> fn){
		val tuple = this;
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new Tuple6<T1,T2,T,T4,T5,T6>(){
			
			public T v3(){
				return value.getOrSet(()->fn.apply(tuple.v3())); 
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	
	/* 
	 * @see com.aol.cyclops.lambda.tuple.Tuple4#map3(java.util.function.Function)
	 */
	default <T> Tuple6<T1,T2,T,T4,T5,T6> map3(Function<T3,T> fn){
		return of(v1(),v2(),fn.apply(v3()),v4(),v5(),v6());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple6<T1,T2,T3,T,T5,T6> lazyMap4(Function<T4,T> fn){
		val tuple = this;
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new Tuple6<T1,T2,T3,T,T5,T6>(){
			
			public T v4(){
				return value.getOrSet(()->fn.apply(tuple.v4())); 
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	/* 
	 * Map element 4
	 * @see com.aol.cyclops.lambda.tuple.Tuple4#map4(java.util.function.Function)
	 */
	default <T> Tuple6<T1,T2,T3,T,T5,T6> map4(Function<T4,T> fn){
		return of(v1(),v2(),v3(),fn.apply(v4()),v5(),v6());
	}
	/*
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple6<T1,T2,T3,T4,T,T6> lazyMap5(Function<T4,T> fn){
		val tuple = this;
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new Tuple6<T1,T2,T3,T4,T,T6>(){
			
			public T v5(){
				return value.getOrSet(()->fn.apply(tuple.v4())); 
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	/**
	 * Map the 6th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple5
	 */
	default <T> Tuple6<T1,T2,T3,T4,T,T6> map5(Function<T5,T> fn){
		return of(v1(),v2(),v3(),v4(),fn.apply(v5()),v6());
	}
	/*
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple6<T1,T2,T3,T4,T5,T> lazyMap6(Function<T4,T> fn){
		val tuple = this;
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new Tuple6<T1,T2,T3,T4,T5,T>(){
			
			public T v6(){
				return value.getOrSet(()->fn.apply(tuple.v4())); 
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	/**
	 * 
	 * Map the 6th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple6
	 */
	default <T> Tuple6<T1,T2,T3,T4,T5,T> map6(Function<T6,T> fn){
		return of(v1(),v2(),v3(),v4(),v5(),fn.apply(v6()));
	}
	
}
