package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

interface Tuple1<T1> extends CachedValues{
	
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
	default <T> Tuple1<T> map1(Function<T1,T> fn){
		return of(fn.apply(v1()));
	}
	default <T> Tuple1<T> flatMap1(Function<T1,Tuple1<T>> fn){
		return fn.apply(v1());
	}
	default Tuple1<T1> swap1(){
		return this;
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==1)
			return Optional.of("(%s,%s)");
		return Optional.empty();
	}
   
	public static <T1> Tuple1<T1> ofTuple(Object tuple1){
		return (Tuple1)new TupleImpl(tuple1,1);
	}
	public static <T1> Tuple1<T1> of(T1 t1){
		return (Tuple1)new TupleImpl(Arrays.asList(t1),1);
	}
	
	
}
