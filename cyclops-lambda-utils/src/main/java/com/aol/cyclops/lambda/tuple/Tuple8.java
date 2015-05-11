package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.functions.OctFunction;

public interface Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> extends Tuple7<T1,T2,T3,T4,T5,T6,T7> {
	
	default T8 v8(){
		if(arity()<8)
			throw new ClassCastException("Attempt to upscale to " + Tuple8.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
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
	default <R> CompletableFuture<R> applyAsync7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> fn){
		return CompletableFuture.completedFuture(v7())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()));
	}
	default <R> CompletableFuture<R>  callAsync(OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> fn, Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6(),i.v7(),i.v8()),e);
	}
	default <R> CompletableFuture<R> applyAsync7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> fn, Executor e){
		return CompletableFuture.completedFuture(v7())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()),e);
	}
	default <T> Tuple8<T1,T2,T3,T4,T5,T6,T7,T> map8(Function<T8,T> fn){
		return of(v1(),v2(),v3(),v4(),v5(),v6(),v7(),fn.apply(v8()));
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
	default Tuple6<T1,T2,T3,T4,T5,T6> tuple6(){
		return this;
	}
	default Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple7(){
		return this;
	}
	default Tuple8<T8,T7,T6,T5,T4,T3,T2,T1> swap8(){
		return of(v8(),v7(),v6(),v5(),v4(),v3(),v2(),v1());
	}
	
	default Optional<String> asStringFormat(int arity){
		if(arity()==8)
			return Optional.of("(%s,%s,%s,%s,%s,%s,%s,%s)");
		return Tuple7.super.asStringFormat(arity);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> ofTuple(Object tuple8){
		return (Tuple8)new TupleImpl(tuple8,8);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
																		T6 t6, T7 t7,T8 t8){
		return (Tuple8)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7,t8),8);
	}
}
