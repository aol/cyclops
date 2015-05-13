package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;




import com.aol.cyclops.comprehensions.functions.QuadFunction;
import com.aol.cyclops.lambda.utils.ImmutableClosedValue;

public interface Tuple4<T1,T2,T3,T4> extends Tuple3<T1,T2,T3> {
	
	default T4 v4(){
		if(arity()<4)
			throw new ClassCastException("Attempt to upscale to " + Tuple4.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
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
	
	
	default Tuple1<T1> tuple1(){
		return this;
	}
	default Tuple2<T1,T2> tuple2(){
		return this;
	}
	default Tuple3<T1,T2,T3> tuple3(){
		return this;
	}
	default Tuple4<T4,T3,T2,T1> swap4(){
		return of(v4(),v3(),v2(),v1());
	}
	
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple4
	 */
	default <T> Tuple4<T,T2,T3,T4> map1(Function<T1,T> fn){
		if(arity()!=4)
			return (Tuple4)Tuple3.super.map1(fn);
		else
			return Tuples.tuple(fn.apply(v1()),v2(),v3(),v4());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> Tuple4<T,T2,T3,T4> lazyMap1(Function<T1,T> fn){
		if(arity()!=4)
			return (Tuple4)Tuple3.super.lazyMap1(fn);
	
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(4){
			public T v1(){
				return value.getOrSet(()->fn.apply(Tuple4.this.v1())); 
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
	default <T> Tuple4<T1,T,T3,T4> lazyMap2(Function<T2,T> fn){
		if(arity()!=4)
			return (Tuple4)Tuple3.super.lazyMap2(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(4){
			
			public T v2(){
				return value.getOrSet(()->fn.apply(Tuple4.this.v2())); 
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
	default <T> Tuple4<T1,T,T3,T4> map2(Function<T2,T> fn){
		if(arity()!=4)
			return (Tuple4)Tuple3.super.map2(fn);
		return of(v1(),fn.apply(v2()),v3(),v4());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> Tuple4<T1,T2,T,T4> lazyMap3(Function<T3,T> fn){
		if(arity()!=4)
			return (Tuple4)Tuple3.super.lazyMap3(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(4){
			
			public T v3(){
				return value.getOrSet(()->fn.apply(Tuple4.this.v3())); 
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
	
	default <T> Tuple4<T1,T2,T,T4> map3(Function<T3,T> fn){
		if(arity()!=4)
			return (Tuple4)Tuple3.super.map3(fn);
		return of(v1(),v2(),fn.apply(v3()),v4());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> Tuple4<T1,T2,T3,T> lazyMap4(Function<T4,T> fn){
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(4){
			
			public T v4(){
				return value.getOrSet(()->fn.apply(Tuple4.this.v4())); 
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
	default <T> Tuple4<T1,T2,T3,T> map4(Function<T4,T> fn){
		return of(v1(),v2(),v3(),fn.apply(v4()));
	}
	
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> ofTuple(Object tuple4){
		return (Tuple4)new TupleImpl(tuple4,4);
	}
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> of(T1 t1, T2 t2,T3 t3,T4 t4){
		return (Tuple4)new TupleImpl(Arrays.asList(t1,t2,t3,t4),4);
	}
}
