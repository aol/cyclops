package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public interface Tuple3<T1,T2,T3> extends Tuple2<T1,T2> {
	
	default T3 v3(){
		if(arity()<3)
			throw new ClassCastException("Attempt to upscale to " + Tuple3.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
		return (T3)getCachedValues().get(2);
	}
	default T3 _3(){
		return v3();
	}

	default T3 getT3(){
		return v3();
	}
	default int arity(){
		return 3;
	}
	default <R> R apply3(Function<T1,Function<T2,Function<T3,R>>> fn){
		return fn.apply(v1()).apply(v2()).apply(v3());
	}
	default <R> CompletableFuture<R> applyAsync3(Function<T1,Function<T2,Function<T3,R>>> fn){
		return CompletableFuture.completedFuture(v3()).thenApplyAsync(fn.apply(v1()).apply(v2()));
	}
	default <T> Tuple3<T1,T2,T> map3(Function<T3,T> fn){
		return of(v1(),v2(),fn.apply(v3()));
	}
	default Tuple1<T1> tuple1(){
		return this;
	}
	default Tuple2<T1,T2> tuple2(){
		return this;
	}
	default Tuple3<T3,T2,T1> swap3(){
		return of(v3(),v2(),v1());
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==3)
			return Optional.of("(%s,%s,%s)");
		return Tuple2.super.asStringFormat(arity);
	}
	
	public static <T1,T2,T3> Tuple3<T1,T2,T3> ofTuple(Object tuple2){
		return (Tuple3)new TupleImpl(tuple2,3);
	}
	public static <T1,T2,T3> Tuple3<T1,T2,T3> of(T1 t1, T2 t2,T3 t3){
		return (Tuple3)new TupleImpl(Arrays.asList(t1,t2,t3),3);
	}
}
