package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import lombok.AllArgsConstructor;

import com.aol.cyclops.functions.TriFunction;

public interface PTuple3<T1,T2,T3> extends PTuple2<T1,T2> {
	
	default T3 v3(){
		if(arity()<3)
			throw new ClassCastException("Attempt to upscale to " + PTuple3.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
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
	default <R> R call(TriFunction<T1,T2,T3,R> fn){
		return fn.apply(v1(),v2(),v3());
	}
	default <R> CompletableFuture<R>  callAsync(TriFunction<T1,T2,T3,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), i.v2(),i.v3()));
	}
	default <R> CompletableFuture<R> applyAsync3(Function<T1,Function<T2,Function<T3,R>>> fn){
		return CompletableFuture.completedFuture(v3()).thenApplyAsync(fn.apply(v1()).apply(v2()));
	}
	default <R> CompletableFuture<R>  callAsync(TriFunction<T1,T2,T3,R> fn,Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), i.v2(),i.v3()),e);
	}
	default <R> CompletableFuture<R> applyAsync3(Function<T1,Function<T2,Function<T3,R>>> fn,Executor e){
		return CompletableFuture.completedFuture(v3()).thenApplyAsync(fn.apply(v1()).apply(v2()),e);
	}
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple1
	 */
	default <T> PTuple3<T,T2,T3> map1(Function<T1,T> fn){
		if(arity()!=3)
			return (PTuple3)PTuple2.super.map1(fn);
		else
			return PowerTuples.tuple(fn.apply(v1()),v2(),v3());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> PTuple3<T,T2,T3> lazyMap1(Function<T1,T> fn){
		if(arity()!=3)
			return (PTuple3)PTuple2.super.lazyMap1(fn);
	
		return new LazyMap1PTuple8(fn,(PTuple8)this);
		
	}
	/**
	 * Lazily Map 2nd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple3<T1,T,T3> lazyMap2(Function<T2,T> fn){
		if(arity()!=3)
			return (PTuple3)PTuple2.super.lazyMap2(fn);
		
		return new LazyMap2PTuple8(fn,(PTuple8)this);
		
	}
	
	/** Map the second element in this Tuple
	 * @param fn mapper function
	 * @return new Tuple3
	 */
	default <T> PTuple3<T1,T,T3> map2(Function<T2,T> fn){
		if(arity()!=3)
			return (PTuple3)PTuple2.super.map2(fn);
		return of(v1(),fn.apply(v2()),v3());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> PTuple3<T1,T2,T> lazyMap3(Function<T3,T> fn){
		
		return new LazyMap3PTuple8(fn,(PTuple8)this);
	}
	
	default <T> PTuple3<T1,T2,T> map3(Function<T3,T> fn){
		return of(v1(),v2(),fn.apply(v3()));
	}
	default PTuple1<T1> tuple2(){
		return (PTuple1<T1>)this.withArity(2);
	}
	
	
	default PTuple3<T3,T2,T1> swap3(){
		return of(v3(),v2(),v1());
	}
	
	default <NT1,NT2,NT3> PTuple3<NT1,NT2,NT3> reorder(Function<PTuple3<T1,T2,T3>,NT1> v1S, Function<PTuple3<T1,T2,T3>,NT2> v2S,Function<PTuple3<T1,T2,T3>,NT3> v3S){
			
		PTuple3<T1,T2,T3> host = this;
			return new TupleImpl(Arrays.asList(),3){
				public NT1 v1(){
					return v1S.apply(host); 
				}
				public NT2 v2(){
					return v2S.apply(host); 
				}

				public NT3 v3(){
					return v3S.apply(host); 
				}
				@Override
				public List<Object> getCachedValues() {
					return Arrays.asList(v1(),v2(),v3());
				}

				@Override
				public Iterator iterator() {
					return getCachedValues().iterator();
				}

				
			};
			
		}
	
	public static ThreeNumbers asThreeNumbers(PTuple3<Number,Number,Number> numbers){
		return new ThreeNumbers(numbers);
	}
	@AllArgsConstructor
	static class ThreeNumbers{
		private final PTuple3 t3;
		public IntStream asRange(){
			int start = ((Number)t3.v1()).intValue();
			int end = ((Number)t3.v2()).intValue();
			int step = ((Number)t3.v3()).intValue();
			
			return IntStream.iterate(start, i -> i + step)
	         .limit((end-start)/step);
			
		}
		public LongStream asLongRange(){
			long start = ((Number)t3.v1()).longValue();
			long end = ((Number)t3.v2()).longValue();
			long step = ((Number)t3.v3()).longValue();
			
			return LongStream.iterate(start, i -> i + step)
	         .limit((end-start)/step);
		}
	}
	default PTuple3<T1,T2,T3> memo(){
		if(arity()!=3)
			return (PTuple3)PTuple2.super.memo();
		PTuple3<T1,T2,T3> host = this;
		Map<Integer,Object> values = new ConcurrentHashMap<>();
		
		return new TupleImpl(Arrays.asList(),3){
			
			
			public T1 v1(){
				return ( T1)values.computeIfAbsent(new Integer(0), key -> host.v1());
			}

			public T2 v2(){
				return ( T2)values.computeIfAbsent(new Integer(1), key -> host.v2());
			}

			public T3 v3(){
				return ( T3)values.computeIfAbsent(new Integer(2), key -> host.v3());
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
	
	public static <T1,T2,T3> PTuple3<T1,T2,T3> ofTuple(Object tuple2){
		return (PTuple3)new TupleImpl(tuple2,3);
	}
	public static <T1,T2,T3> PTuple3<T1,T2,T3> of(T1 t1, T2 t2,T3 t3){
		return (PTuple3)new TupleImpl(Arrays.asList(t1,t2,t3),3);
	}
}
