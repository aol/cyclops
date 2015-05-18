package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;

import lombok.val;

import com.aol.cyclops.functions.OctFunction;
import com.aol.cyclops.lambda.utils.LazyImmutable;

public interface PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> extends PTuple7<T1,T2,T3,T4,T5,T6,T7> {
	
	default T8 v8(){
		if(arity()<8)
			throw new ClassCastException("Attempt to upscale to " + PTuple8.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
		return (T8)getCachedValues().get(7);
	}
	default T8 _8(){
		return v8();
	}

	default T8 getT8(){
		return v8();
	}
	default int arity(){
		return 8;
	}
	default <R> R apply8(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  fn){
		return fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()).apply(v7()).apply(v8());
	}
	
	default <R> CompletableFuture<R> applyAsync8(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  fn){
		return CompletableFuture.completedFuture(v8())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()).apply(v7()));
	}
	default <R> R call(OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> fn){
		return fn.apply(v1(),v2(),v3(),v4(),v5(),v6(),v7(),v8());
	}
	default <R> CompletableFuture<R>  callAsync(OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6(),i.v7(),i.v8()));
	}
	
	default <R> CompletableFuture<R>  callAsync(OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> fn, Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6(),i.v7(),i.v8()),e);
	}
	default <R> CompletableFuture<R> applyAsync8(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Function<T8,R>>>>>>>>  fn, Executor e){
		return CompletableFuture.completedFuture(v8())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()).apply(v7()),e);
	}
	/**
	default PTuple1<T1> tuple1(){
		return this;
	}
	default PTuple2<T1,T2> tuple2(){
		return this;
	}
	default PTuple3<T1,T2,T3> tuple3(){
		return this;
	}
	default PTuple4<T1,T2,T3,T4> tuple4(){
		return this;
	}
	default PTuple5<T1,T2,T3,T4,T5> tuple5(){
		return this;
	}
	default PTuple6<T1,T2,T3,T4,T5,T6> tuple6(){
		return this;
	}**/
	default PTuple7<T1,T2,T3,T4,T5,T6,T7> Tuple7(){
		return (PTuple7)withArity(7);
	}
	default PTuple8<T8,T7,T6,T5,T4,T3,T2,T1> swap8(){
		return of(v8(),v7(),v6(),v5(),v4(),v3(),v2(),v1());
	}
	

	
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple8
	 */
	default <T> PTuple8<T,T2,T3,T4,T5,T6,T7,T8> map1(Function<T1,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map1(fn);
		else
			return PowerTuples.tuple(fn.apply(v1()),v2(),v3(),v4(),v5(),v6(),v7(),v8());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T,T2,T3,T4,T5,T6,T7,T8> lazyMap1(Function<T1,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap1(fn);
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			public T v1(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v1())); 
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
	default <T> PTuple8<T1,T,T3,T4,T5,T6,T7,T8> lazyMap2(Function<T2,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap2(fn);
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl<T1,T,T3,T4,T5,T6,T7,T8>(8){
			
			public T v2(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v2())); 
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
	default <T> PTuple8<T1,T,T3,T4,T5,T6,T7,T8> map2(Function<T2,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map2(fn);
		else
			return of(v1(),fn.apply(v2()),v3(),v4(),v5(),v6(),v7(),v8());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T1,T2,T,T4,T5,T6,T7,T8> lazyMap3(Function<T3,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap3(fn);
		
	
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			
			public T v3(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v3())); 
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
	default <T> PTuple8<T1,T2,T,T4,T5,T6,T7,T8> map3(Function<T3,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map3(fn);
		
		return of(v1(),v2(),fn.apply(v3()),v4(),v5(),v6(),v7(),v8());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T1,T2,T3,T,T5,T6,T7,T8> lazyMap4(Function<T4,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap4(fn);
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			
			public T v4(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v4())); 
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
	default <T> PTuple8<T1,T2,T3,T,T5,T6,T7,T8> map4(Function<T4,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map4(fn);
		
		return of(v1(),v2(),v3(),fn.apply(v4()),v5(),v6(),v7(),v8());
	}
	/*
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T1,T2,T3,T4,T,T6,T7,T8> lazyMap5(Function<T5,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap5(fn);
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			
			public T v5(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v5())); 
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
	default <T> PTuple8<T1,T2,T3,T4,T,T6,T7,T8> map5(Function<T5,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map5(fn);
		
		return of(v1(),v2(),v3(),v4(),fn.apply(v5()),v6(),v7(),v8());
	}
	/*
	 * Lazily Map 6th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T1,T2,T3,T4,T5,T,T7,T8> lazyMap6(Function<T6,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap6(fn);
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			
			public T v6(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v6())); 
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
	 * @return new Tuple8
	 */
	default <T> PTuple8<T1,T2,T3,T4,T5,T,T7,T8> map6(Function<T6,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map6(fn);
		
		return of(v1(),v2(),v3(),v4(),v5(),fn.apply(v6()),v7(),v8());
	}
	/*
	 * Lazily Map 7th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T1,T2,T3,T4,T5,T6,T,T8> lazyMap7(Function<T7,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.lazyMap7(fn);
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			
			public T v7(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v7())); 
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
	 * Map the 7th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple8
	 */
	default <T> PTuple8<T1,T2,T3,T4,T5,T6,T,T8> map7(Function<T7,T> fn){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.map7(fn);
		
		return of(v1(),v2(),v3(),v4(),v5(),v6(),fn.apply(v7()),v8());
	}
	/*
	 * Lazily Map 8th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple8<T1,T2,T3,T4,T5,T6,T7,T> lazyMap8(Function<T8,T> fn){
		
		
		
		LazyImmutable<T> value = new LazyImmutable<>();
		return new TupleImpl(8){
			
			public T v8(){
				return value.getOrSet(()->fn.apply(PTuple8.this.v8())); 
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
	 * Map the 8th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple8
	 */
	default <T> PTuple8<T1,T2,T3,T4,T5,T6,T7,T> map8(Function<T8,T> fn){
		return of(v1(),v2(),v3(),v4(),v5(),v6(),v7(),fn.apply(v8()));
	}
	/**
	 * Lazily reorder a PTuple8
	 * 
	 * @param v1S Function that determines new first element
	 * @param v2S Function that determines new second element
	 * @param v3S Function that determines new third element
	 * @param v4S Function that determines new fourth element
	 * @param v5S Function that determines new fifth element
	 * @param v6S Function that determines new sixth element
	 * @param v7S Function that determines new seventh element
	 * @param v8S Function that determines new eighth element
	 * @return reordered PTuple8
	 */
	default <NT1, NT2, NT3, NT4,NT5,NT6,NT7,NT8> PTuple8<NT1, NT2, NT3, NT4,NT5,NT6,NT7,NT8> reorder(
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT1> v1S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT2> v2S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT3> v3S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT4> v4S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT5> v5S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT6> v6S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT7> v7S,
			Function<PTuple8<T1, T2, T3, T4,T5,T6,T7,T8>, NT8> v8S) {

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

			public NT6 v6() {
				return v6S.apply(host);
			}
			public NT7 v7() {
				return v7S.apply(host);
			}

			public NT8 v8() {
				return v8S.apply(host);
			}

			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(), v2(), v3(), v4(),v5(),v6(),v7(),v8());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

		};

	}
	default PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> memo(){
		if(arity()!=8)
			return (PTuple8)PTuple7.super.memo();
		val host = this;
		Map<Integer,Object> values = new ConcurrentHashMap<>();
		
		return new TupleImpl(Arrays.asList(),8){
			
			
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

			public T7 v7(){
				return ( T7)values.computeIfAbsent(new Integer(6), key -> host.v7());
			}

			public T8 v8(){
				return ( T8)values.computeIfAbsent(new Integer(7), key -> host.v8());
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4(),v5(),v6(),v7(),v8());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	
	public static <T1,T2,T3,T4,T5,T6,T7,T8> PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> ofTuple(Object tuple8){
		return (PTuple8)new TupleImpl(tuple8,8);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
																		T6 t6, T7 t7,T8 t8){
		return (PTuple8)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7,t8),8);
	}
}
