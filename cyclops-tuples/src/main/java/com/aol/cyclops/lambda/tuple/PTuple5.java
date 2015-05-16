package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;





import lombok.val;

import com.aol.cyclops.comprehensions.functions.QuintFunction;
import com.aol.cyclops.lambda.utils.ImmutableClosedValue;

public interface PTuple5<T1,T2,T3,T4,T5> extends PTuple4<T1,T2,T3,T4> {
	
	default T5 v5(){
		if(arity()<5)
			throw new ClassCastException("Attempt to upscale to " + PTuple5.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
		return (T5)getCachedValues().get(4);
	}
	default T5 _5(){
		return v5();
	}

	default T5 getT5(){
		return v5();
	}
	default int arity(){
		return 5;
	}
	default <R> R apply5(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> fn){
		return fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5());
	}
	default <R> R call(QuintFunction<T1,T2,T3,T4,T5,R> fn){
		return fn.apply(v1(),v2(),v3(),v4(),v5());
	}
	default <R> CompletableFuture<R>  callAsync(QuintFunction<T1,T2,T3,T4,T5,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5()));
	}
	default <R> CompletableFuture<R> applyAsync5(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> fn){
		return CompletableFuture.completedFuture(v5())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()));
	}
	default <R> CompletableFuture<R>  callAsync(QuintFunction<T1,T2,T3,T4,T5,R> fn, Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5()),e);
	}
	default <R> CompletableFuture<R> applyAsync5(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,R>>>>> fn, Executor e){
		return CompletableFuture.completedFuture(v5())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()),e);
	}
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple5
	 */
	default <T> PTuple5<T,T2,T3,T4,T5> map1(Function<T1,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.map1(fn);
		else
			return PowerTuples.tuple(fn.apply(v1()),v2(),v3(),v4(),v5());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple5<T,T2,T3,T4,T5> lazyMap1(Function<T1,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.lazyMap1(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(5){
			public T v1(){
				return value.getOrSet(()->fn.apply(PTuple5.this.v1())); 
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple5<T1,T,T3,T4,T5> lazyMap2(Function<T2,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.lazyMap2(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(5){
			
			public T v2(){
				return value.getOrSet(()->fn.apply(PTuple5.this.v2())); 
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
	default <T> PTuple5<T1,T,T3,T4,T5> map2(Function<T2,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.map2(fn);
		return of(v1(),fn.apply(v2()),v3(),v4(),v5());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple5<T1,T2,T,T4,T5> lazyMap3(Function<T3,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.lazyMap3(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(5){
			
			public T v3(){
				return value.getOrSet(()->fn.apply(PTuple5.this.v3())); 
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
	default <T> PTuple5<T1,T2,T,T4,T5> map3(Function<T3,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.map3(fn);
		return of(v1(),v2(),fn.apply(v3()),v4(),v5());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple5<T1,T2,T3,T,T5> lazyMap4(Function<T4,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.lazyMap4(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(5){
			
			public T v4(){
				return value.getOrSet(()->fn.apply(PTuple5.this.v4())); 
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
	default <T> PTuple5<T1,T2,T3,T,T5> map4(Function<T4,T> fn){
		if(arity()!=5)
			return (PTuple5)PTuple4.super.map4(fn);
		return of(v1(),v2(),v3(),fn.apply(v4()),v5());
	}
	/**
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple5<T1,T2,T3,T4,T> lazyMap5(Function<T5,T> fn){
	
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(5){
			
			public T v5(){
				return value.getOrSet(()->fn.apply(PTuple5.this.v5())); 
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
	 * Map the 5th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple5
	 */
	default <T> PTuple5<T1,T2,T3,T4,T> map5(Function<T5,T> fn){
		return of(v1(),v2(),v3(),v4(),fn.apply(v5()));
	}

	default PTuple4<T1,T2,T3,T4> tuple4(){
		return (PTuple4)withArity(4);
	} 
	default PTuple5<T5,T4,T3,T2,T1> swap5(){
		return of(v5(),v4(),v3(),v2(),v1());
	}

	/**
	 * Lazily reorder a PTuple5 or both a narrow and reorder a larger Tuple
	 * 
	 * @param v1S
	 * @param v2S
	 * @param v3S
	 * @param v4S
	 * @param v5S
	 * @return
	 */
	default <NT1, NT2, NT3, NT4,NT5> PTuple5<NT1, NT2, NT3, NT4,NT5> reorder(
			Function<PTuple5<T1, T2, T3, T4,T5>, NT1> v1S,
			Function<PTuple5<T1, T2, T3, T4,T5>, NT2> v2S,
			Function<PTuple5<T1, T2, T3, T4,T5>, NT3> v3S,
			Function<PTuple5<T1, T2, T3, T4,T5>, NT4> v4S,
			Function<PTuple5<T1, T2, T3, T4,T5>, NT5> v5S) {

		val host = this;
		return new TupleImpl(Arrays.asList(), 5) {
			public NT1 v1() {
				return v1S.apply(host);
			}

			public NT2 v2() {
				return v2S.apply(host);
			}

			public NT3 v3() {
				return v3S.apply(host);
			}

			public NT4 v4() {
				return v4S.apply(host);
			}
			public NT5 v5() {
				return v5S.apply(host);
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(), v2(), v3(), v4(),v5());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

		};

}
	public static <T1,T2,T3,T4,T5> PTuple5<T1,T2,T3,T4,T5> ofTuple(Object tuple5){
		return (PTuple5)new TupleImpl(tuple5,5);
	}
	public static <T1,T2,T3,T4,T5> PTuple5<T1,T2,T3,T4,T5> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5){
		return (PTuple5)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5),5);
	}
}
