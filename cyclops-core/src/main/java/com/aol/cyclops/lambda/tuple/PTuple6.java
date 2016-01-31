package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.functions.HexFunction;
import com.aol.cyclops.lambda.tuple.lazymap.LazyMap1PTuple8;
import com.aol.cyclops.lambda.tuple.lazymap.LazyMap2PTuple8;
import com.aol.cyclops.lambda.tuple.lazymap.LazyMap3PTuple8;
import com.aol.cyclops.lambda.tuple.lazymap.LazyMap4PTuple8;
import com.aol.cyclops.lambda.tuple.lazymap.LazyMap5PTuple8;
import com.aol.cyclops.lambda.tuple.lazymap.LazyMap6PTuple8;

public interface PTuple6<T1,T2,T3,T4,T5,T6> extends PTuple5<T1,T2,T3,T4,T5> {
	
	default T6 v6(){
		if(arity()<6)
			throw new ClassCastException("Attempt to upscale to " + PTuple6.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
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
	
	default PTuple5<T1,T2,T3,T4,T5> tuple5(){
		return (PTuple5)withArity(5);
	}
	default PTuple6<T6,T5,T4,T3,T2,T1> swap6(){
		return of(v6(),v5(),v4(),v3(),v2(),v1());
	}
	
	public static <T1,T2,T3,T4,T5,T6> PTuple6<T1,T2,T3,T4,T5,T6> ofTuple(Object tuple6){
		return (PTuple6)new TupleImpl(tuple6,6);
	}
	public static <T1,T2,T3,T4,T5,T6> PTuple6<T1,T2,T3,T4,T5,T6> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5, T6 t6){
		return (PTuple6)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6),6);
	}
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple6
	 */
	default <T> PTuple6<T,T2,T3,T4,T5,T6> map1(Function<T1,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.map1(fn);
		else
			return PowerTuples.tuple(fn.apply(v1()),v2(),v3(),v4(),v5(),v6());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple6<T,T2,T3,T4,T5,T6> lazyMap1(Function<T1,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.lazyMap1(fn);
		
		return new LazyMap1PTuple8(fn,(PTuple8)this);
		
	}
	/**
	 * Lazily Map 2nd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple6<T1,T,T3,T4,T5,T6> lazyMap2(Function<T2,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.lazyMap2(fn);
		
		return new LazyMap2PTuple8(fn,(PTuple8)this);
		
	}
	
	/** Map the second element in this Tuple
	 * @param fn mapper function
	 * @return new Tuple3
	 */
	default <T> PTuple6<T1,T,T3,T4,T5,T6> map2(Function<T2,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.map2(fn);
		return of(v1(),fn.apply(v2()),v3(),v4(),v5(),v6());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple6<T1,T2,T,T4,T5,T6> lazyMap3(Function<T3,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.lazyMap3(fn);
	
		return new LazyMap3PTuple8(fn,(PTuple8)this);
		
	}
	
	/* 
	 * @see com.aol.cyclops.lambda.tuple.Tuple4#map3(java.util.function.Function)
	 */
	default <T> PTuple6<T1,T2,T,T4,T5,T6> map3(Function<T3,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.map3(fn);
		return of(v1(),v2(),fn.apply(v3()),v4(),v5(),v6());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple6<T1,T2,T3,T,T5,T6> lazyMap4(Function<T4,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.lazyMap4(fn);
		
		return new LazyMap4PTuple8(fn,(PTuple8)this);
		
	}
	/* 
	 * Map element 4
	 * @see com.aol.cyclops.lambda.tuple.Tuple4#map4(java.util.function.Function)
	 */
	default <T> PTuple6<T1,T2,T3,T,T5,T6> map4(Function<T4,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.map4(fn);
		return of(v1(),v2(),v3(),fn.apply(v4()),v5(),v6());
	}
	/*
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple6<T1,T2,T3,T4,T,T6> lazyMap5(Function<T5,T> fn){
			if(arity()!=6)
				return (PTuple6)PTuple5.super.lazyMap5(fn);

		return new LazyMap5PTuple8(fn,(PTuple8)this);
		
	}
	/**
	 * Map the 6th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple5
	 */
	default <T> PTuple6<T1,T2,T3,T4,T,T6> map5(Function<T5,T> fn){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.map5(fn);
		return of(v1(),v2(),v3(),v4(),fn.apply(v5()),v6());
	}
	/*
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple6<T1,T2,T3,T4,T5,T> lazyMap6(Function<T6,T> fn){

		return new LazyMap6PTuple8(fn,(PTuple8)this);
		
	}
	/**
	 * 
	 * Map the 6th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple6
	 */
	default <T> PTuple6<T1,T2,T3,T4,T5,T> map6(Function<T6,T> fn){
		return of(v1(),v2(),v3(),v4(),v5(),fn.apply(v6()));
	}
	/**
	 * Lazily reorder a PTuple6 or both a narrow and reorder a larger Tuple
	 * 
	 * @param v1S Function that determines new first element
	 * @param v2S Function that determines new second element
	 * @param v3S Function that determines new third element
	 * @param v4S Function that determines new fourth element
	 * @param v5S Function that determines new fifth element
	 * @param v6S Function that determines new sixth element
	 * @return reordered PTuple6
	 */
	default <NT1, NT2, NT3, NT4,NT5,NT6> PTuple6<NT1, NT2, NT3, NT4,NT5,NT6> reorder(
			Function<PTuple6<T1, T2, T3, T4,T5,T6>, NT1> v1S,
			Function<PTuple6<T1, T2, T3, T4,T5,T6>, NT2> v2S,
			Function<PTuple6<T1, T2, T3, T4,T5,T6>, NT3> v3S,
			Function<PTuple6<T1, T2, T3, T4,T5,T6>, NT4> v4S,
			Function<PTuple6<T1, T2, T3, T4,T5,T6>, NT5> v5S,
			Function<PTuple6<T1, T2, T3, T4,T5,T6>, NT6> v6S) {

		PTuple6<T1,T2,T3,T4,T5,T6> host = this;
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

			public NT6 v6() {
				return v6S.apply(host);
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(), v2(), v3(), v4(),v5(),v6());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

		};

}
	default PTuple6<T1,T2,T3,T4,T5,T6> memo(){
		if(arity()!=6)
			return (PTuple6)PTuple5.super.memo();
		PTuple6<T1,T2,T3,T4,T5,T6> host = this;
		Map<Integer,Object> values = new ConcurrentHashMap<>();
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T1 v1(){
				return ( T1)values.computeIfAbsent(new Integer(0), key -> host.v1());
			}

			public T2 v2(){
				return ( T2)values.computeIfAbsent(new Integer(1), key -> host.v2());
			}

			public T3 v3(){
				return ( T3)values.computeIfAbsent(new Integer(2), key -> host.v3());
			}

			public T4 v4(){
				return ( T4)values.computeIfAbsent(new Integer(3), key -> host.v4());
			}

			public T5 v5(){
				return ( T5)values.computeIfAbsent(new Integer(4), key -> host.v5());
			}

			public T6 v6(){
				return ( T6)values.computeIfAbsent(new Integer(5), key -> host.v6());
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4(),v5(),v6());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	
}
