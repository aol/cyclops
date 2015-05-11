package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
	default <R> CompletableFuture<R> applyAsync4(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> fn){
		return CompletableFuture.completedFuture(v4())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()));
	}
	default <T> Tuple4<T1,T2,T3,T> map4(Function<T4,T> fn){
		return of(v1(),v2(),v3(),fn.apply(v4()));
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
	default Optional<String> asStringFormat(int arity){
		if(arity()==4)
			return Optional.of("(%s,%s,%s,%s)");
		return Tuple3.super.asStringFormat(arity);
	}
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> ofTuple(Object tuple4){
		return (Tuple4)new TupleImpl(tuple4,4);
	}
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> of(T1 t1, T2 t2,T3 t3,T4 t4){
		return (Tuple4)new TupleImpl(Arrays.asList(t1,t2,t3,t4),4);
	}
}
