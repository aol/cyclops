package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public interface PTuple1<T1> extends CachedValues{
	
	default T1 v1(){
		return (T1)getCachedValues().get(0);
	}
	default T1 _1(){
		return v1();
	}

	default T1 getT1(){
		return v1();
	}
	
	default int arity(){
		return 1;
	}
	default <T> T apply1(Function<T1,T> fn){
		return fn.apply(v1());
	}
	default <T> T call(Function<T1,T> fn){
		return fn.apply(v1());
	}
	default <T> CompletableFuture<T>  callAsync(Function<T1,T> fn){
		return CompletableFuture.completedFuture(v1()).thenApplyAsync(fn);
	}
	default <T> CompletableFuture<T> applyAsync1(Function<T1,T> fn){
		return CompletableFuture.completedFuture(v1()).thenApplyAsync(fn);
	}
	default <T> CompletableFuture<T>  callAsync(Function<T1,T> fn,Executor e){
		return CompletableFuture.completedFuture(v1()).thenApplyAsync(fn,e);
	}
	default <T> CompletableFuture<T> applyAsync1(Function<T1,T> fn,Executor e){
		return CompletableFuture.completedFuture(v1()).thenApplyAsync(fn,e);
	}
	
	
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple1
	 */
	default <T> PTuple1<T> map1(Function<T1,T> fn){
		return PowerTuples.tuple(fn.apply(v1()));
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple1<T> lazyMap1(Function<T1,T> fn){
		
		return new LazyMap1PTuple8(fn,(PTuple8)this);
	}
	
	default <NT1> PTuple1<NT1> reorder(Function<PTuple1<T1>,NT1> v1S){

		return new ReorderP1<>(v1S, this);
	}
	
	
	default PTuple1<T1> swap1(){
		return PowerTuples.tuple(v1());
	}
	
	default PTuple1<T1> memo(){
			return new Memo1<>( this);
	}
	public static <T1> PTuple1<T1> ofTuple(Object tuple1){
		return (PTuple1)new TupleImpl(tuple1,1);
	}
	public static <T1> PTuple1<T1> of(T1 t1){
		return (PTuple1)new TupleImpl(Arrays.asList(t1),1);
	}



}
