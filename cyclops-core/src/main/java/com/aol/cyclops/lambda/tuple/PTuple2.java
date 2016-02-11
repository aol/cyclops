package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import lombok.AllArgsConstructor;

public interface PTuple2<T1,T2> extends PTuple1<T1>{
	
	default T2 v2(){
		if(arity()<2)
			throw new ClassCastException("Attempt to upscale to " + PTuple2.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());		return (T2)getCachedValues().get(1);
	}
	default T2 _2(){
		return v2();
	}

	default T2 getT2(){
		return v2();
	}
	
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple1
	 */
	default <T> PTuple2<T,T2> map1(Function<T1,T> fn){
		if(arity()!=2)
			return (PTuple2)PTuple1.super.map1(fn);
		else
			return PowerTuples.tuple(fn.apply(v1()),v2());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple2<T,T2> lazyMap1(Function<T1,T> fn){
		if(arity()!=2)
			return (PTuple2)PTuple1.super.lazyMap1(fn);
		
		return new LazyMap1PTuple8(fn,(PTuple8)this);
		
	}
	/**
	 * Lazily Map 2nd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple2<T1,T> lazyMap2(Function<T2,T> fn){
		
		return new LazyMap2PTuple8(fn,(PTuple8)this);
		
	}
	
	/** Map the second element in this Tuple
	 * @param fn mapper function
	 * @return new Tuple2
	 */
	default <T> PTuple2<T1,T> map2(Function<T2,T> fn){
		return of(v1(),fn.apply(v2()));
	}
	default PTuple1<T1> tuple1(){
		return (PTuple1<T1>)this.withArity(1);
	}
	default <NT1,NT2> PTuple2<NT1,NT2> reorder(Function<PTuple2<T1,T2>,NT1> v1S, Function<PTuple2<T1,T2>,NT2> v2S){
		
		PTuple2<T1,T2> host = this;
			return new TupleImpl(Arrays.asList(),2){
				public NT1 v1(){
					return v1S.apply(host); 
				}
				public NT2 v2(){
					return v2S.apply(host); 
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
	
	
	default PTuple2<T2,T1> swap2(){
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
	
	
	default int arity(){
		return 2;
	}
	
	public static TwoNumbers asTwoNumbers(PTuple2<Number,Number> tuple){
		return new TwoNumbers(tuple);
	}
	@AllArgsConstructor
	static class TwoNumbers{
		private final PTuple2 t2;
		public IntStream asRange(){
			//check if int

			//if not toString and then as int
			return IntStream.range(((Number)t2.v1()).intValue(), ((Number)t2.v1()).intValue());
		}
		public LongStream asLongRange(){
			return LongStream.range(((Number)t2.v1()).longValue(), ((Number)t2.v1()).longValue());
		}
	}
	default PTuple2<T1,T2> memo(){
		if(arity()!=2)
			return (PTuple2)PTuple1.super.memo();
		
		
		
		return new Memo2(this);
		
	}
	public static <T1,T2> PTuple2<T1,T2> ofTuple(Object tuple2){
		return (PTuple2)new TupleImpl(tuple2,2);
	}
	public static <T1,T2> PTuple2<T1,T2> of(T1 t1, T2 t2){
		return (PTuple2)new TupleImpl(Arrays.asList(t1,t2),2);
	}


}
