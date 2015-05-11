package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.functions.HexFunction;

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
	default <T> Tuple6<T1,T2,T3,T4,T5,T> map6(Function<T6,T> fn){
		return of(v1(),v2(),v3(),v4(),v5(),fn.apply(v6()));
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
}
