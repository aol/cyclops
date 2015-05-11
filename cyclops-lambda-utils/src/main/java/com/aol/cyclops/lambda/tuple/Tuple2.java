package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import lombok.AllArgsConstructor;

public interface Tuple2<T1,T2> extends Tuple1<T1> {
	
	default T2 v2(){
		if(arity()<2)
			throw new ClassCastException("Attempt to upscale to " + Tuple2.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());		return (T2)getCachedValues().get(1);
	}
	default T2 _2(){
		return v2();
	}

	default T2 getT2(){
		return v2();
	}
	
	default Tuple1<T1> tuple1(){
		return this;
	}
	
	default Tuple2<T2,T1> swap2(){
		return of(v2(),v1());
	}
	
	default <R> R apply2(Function<T1,Function<T2,R>> fn){
		return fn.apply(v1()).apply(v2());
	}
	default <R> R call(BiFunction<T1,T2,R> fn){
		return fn.apply(v1(),v2());
	}
	default <R> CompletableFuture<R>  callAsync(BiFunction<T1,T2,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), i.v2()));
	}
	default <R> CompletableFuture<R> applyAsync2(Function<T1,Function<T2,R>> fn){
		return CompletableFuture.completedFuture(v2()).thenApplyAsync(fn.apply(v1()));
	}
	default <R> CompletableFuture<R>  callAsync(BiFunction<T1,T2,R> fn,Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), i.v2()),e);
	}
	default <R> CompletableFuture<R> applyAsync2(Function<T1,Function<T2,R>> fn,Executor e){
		return CompletableFuture.completedFuture(v2()).thenApplyAsync(fn.apply(v1()),e);
	}
	default <T> Tuple2<T1,T> map2(Function<T2,T> fn){
		return of(v1(),fn.apply(v2()));
	}
	
	default int arity(){
		return 2;
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==2)
			return Optional.of("(%s,%s)");
		return Tuple1.super.asStringFormat(arity);
	}
	default TwoNumbers asTwoNumbers(){
		return new TwoNumbers(this);
	}
	@AllArgsConstructor
	static class TwoNumbers{
		private final Tuple2 t2;
		public IntStream asRange(){
			//check if int

			//if not toString and then as int
			return IntStream.range(((Number)t2.v1()).intValue(), ((Number)t2.v1()).intValue());
		}
		public LongStream asLongRange(){
			return LongStream.range(((Number)t2.v1()).longValue(), ((Number)t2.v1()).longValue());
		}
	}
	public static <T1,T2> Tuple2<T1,T2> ofTuple(Object tuple2){
		return (Tuple2)new TupleImpl(tuple2,2);
	}
	public static <T1,T2> Tuple2<T1,T2> of(T1 t1, T2 t2){
		return (Tuple2)new TupleImpl(Arrays.asList(t1,t2),2);
	}
}
