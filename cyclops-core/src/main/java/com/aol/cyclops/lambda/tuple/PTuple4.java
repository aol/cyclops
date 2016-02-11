package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.functions.QuadFunction;

public interface PTuple4<T1,T2,T3,T4> extends PTuple3<T1,T2,T3> {
	
	default T4 v4(){
		if(arity()<4)
			throw new ClassCastException("Attempt to upscale to " + PTuple4.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
		return (T4)getCachedValues().get(3);
	}
	default T4 _4(){
		return v4();
	}

	default T4 getT4(){
		return v4();
	}
	
	default int arity(){
		return 4;
	}
	default <R> R apply4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> fn){
		return fn.apply(v1()).apply(v2()).apply(v3()).apply(v4());
	}
	default <R> R call(QuadFunction<T1,T2,T3,T4,R> fn){
		return fn.apply(v1(),v2(),v3(),v4());
	}
	default <R> CompletableFuture<R>  callAsync(QuadFunction<T1,T2,T3,T4,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4()));
	}
	default <R> CompletableFuture<R> applyAsync4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> fn){
		return CompletableFuture.completedFuture(v4())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()));
	}
	default <R> CompletableFuture<R>  callAsync(QuadFunction<T1,T2,T3,T4,R> fn,Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4()),e);
	}
	default <R> CompletableFuture<R> applyAsync4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> fn, Executor e){
		return CompletableFuture.completedFuture(v4())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()),e);
	}
	
	
	default <NT1,NT2,NT3,NT4> PTuple4<NT1,NT2,NT3,NT4> reorder(Function<PTuple4<T1,T2,T3,T4>,NT1> v1S, Function<PTuple4<T1,T2,T3,T4>,NT2> v2S,
										Function<PTuple4<T1,T2,T3,T4>,NT3> v3S,Function<PTuple4<T1,T2,T3,T4>,NT4> v4S){
		
		PTuple4<T1,T2,T3,T4> host = this;
			return new TupleImpl(Arrays.asList(),4){
				public NT1 v1(){
					return v1S.apply(host); 
				}
				public NT2 v2(){
					return v2S.apply(host); 
				}

				public NT3 v3(){
					return v3S.apply(host); 
				}
				public NT4 v4(){
					return v4S.apply(host); 
				}
				@Override
				public List<Object> getCachedValues() {
					return Arrays.asList(v1(),v2(),v3(),v4());
				}

				@Override
				public Iterator iterator() {
					return getCachedValues().iterator();
				}

				
			};
			
		}
	default PTuple3<T1,T2,T3> tuple3(){
		return (PTuple3<T1,T2,T3>)withArity(3);
	}
	default PTuple4<T4,T3,T2,T1> swap4(){
		return of(v4(),v3(),v2(),v1());
	}
	
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple4
	 */
	default <T> PTuple4<T,T2,T3,T4> map1(Function<T1,T> fn){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.map1(fn);
		else
			return PowerTuples.tuple(fn.apply(v1()),v2(),v3(),v4());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple4<T,T2,T3,T4> lazyMap1(Function<T1,T> fn){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.lazyMap1(fn);
	
		return new LazyMap1PTuple8(fn,(PTuple8)this);
		
	}
	/**
	 * Lazily Map 2nd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple4<T1,T,T3,T4> lazyMap2(Function<T2,T> fn){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.lazyMap2(fn);
		
		return new LazyMap2PTuple8(fn,(PTuple8)this);
		
	}
	
	/** Map the second element in this Tuple
	 * @param fn mapper function
	 * @return new Tuple3
	 */
	default <T> PTuple4<T1,T,T3,T4> map2(Function<T2,T> fn){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.map2(fn);
		return of(v1(),fn.apply(v2()),v3(),v4());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple4<T1,T2,T,T4> lazyMap3(Function<T3,T> fn){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.lazyMap3(fn);
		
		return new LazyMap3PTuple8(fn,(PTuple8)this);
		
	}
	
	default <T> PTuple4<T1,T2,T,T4> map3(Function<T3,T> fn){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.map3(fn);
		return of(v1(),v2(),fn.apply(v3()),v4());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple4<T1,T2,T3,T> lazyMap4(Function<T4,T> fn){
		
		return new LazyMap4PTuple8(fn,(PTuple8)this);
		
	}
	default <T> PTuple4<T1,T2,T3,T> map4(Function<T4,T> fn){
		return of(v1(),v2(),v3(),fn.apply(v4()));
	}
	default PTuple4<T1,T2,T3,T4> memo(){
		if(arity()!=4)
			return (PTuple4)PTuple3.super.memo();
		PTuple4<T1,T2,T3,T4> host = this;
		Map<Integer,Object> values = new ConcurrentHashMap<>();
		
		return new TupleImpl(Arrays.asList(),4){
			
			
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


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	public static <T1,T2,T3,T4> PTuple4<T1,T2,T3,T4> ofTuple(Object tuple4){
		return (PTuple4)new TupleImpl(tuple4,4);
	}
	public static <T1,T2,T3,T4> PTuple4<T1,T2,T3,T4> of(T1 t1, T2 t2,T3 t3,T4 t4){
		return (PTuple4)new TupleImpl(Arrays.asList(t1,t2,t3,t4),4);
	}
}
