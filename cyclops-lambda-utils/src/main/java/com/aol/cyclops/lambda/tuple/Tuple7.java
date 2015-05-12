package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;




import com.aol.cyclops.comprehensions.functions.HeptFunction;
import com.aol.cyclops.lambda.utils.ImmutableClosedValue;

public interface Tuple7<T1,T2,T3,T4,T5,T6,T7> extends Tuple6<T1,T2,T3,T4,T5,T6> {
	
	default T7 v7(){
		if(arity()<7)
			throw new ClassCastException("Attempt to upscale to " + Tuple7.class.getCanonicalName() + " from com.aol.cyclops.lambda.tuple.Tuple"+arity());
		return (T7)getCachedValues().get(6);
	}
	default T7 _7(){
		return v7();
	}

	default T7 getT7(){
		return v7();
	}
	default int arity(){
		return 7;
	}
	default <R> R apply7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> fn){
		return fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()).apply(v7());
	}
	default <R> R call(HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> fn){
		return fn.apply(v1(),v2(),v3(),v4(),v5(),v6(),v7());
	}
	default <R> CompletableFuture<R>  callAsync(HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> fn){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6(),i.v7()));
	}
	default <R> CompletableFuture<R> applyAsync7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> fn){
		return CompletableFuture.completedFuture(v7())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()));
	}
	default <R> CompletableFuture<R>  callAsync(HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> fn, Executor e){
		return CompletableFuture.completedFuture(this).thenApplyAsync(i->fn.apply(i.v1(), 
				i.v2(),i.v3(),i.v4(),i.v5(),i.v6(),i.v7()),e);
	}
	default <R> CompletableFuture<R> applyAsync7(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,R>>>>>>> fn,
								Executor e){
		return CompletableFuture.completedFuture(v7())
				.thenApplyAsync(fn.apply(v1()).apply(v2()).apply(v3()).apply(v4()).apply(v5()).apply(v6()),e);
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
	default Tuple6<T1,T2,T3,T4,T5,T6> Tuple7(){
		return this;
	}
	default Tuple7<T7,T6,T5,T4,T3,T2,T1> swap7(){
		return of(v7(),v6(),v5(),v4(),v3(),v2(),v1());
	}
	default Optional<String> asStringFormat(int arity){
		if(arity()==7)
			return Optional.of("(%s,%s,%s,%s,%s,%s,%s)");
		return Tuple6.super.asStringFormat(arity);
	}
	
	
	/**Strict mapping of the first element
	 * 
	 * @param fn Mapping function
	 * @return Tuple7
	 */
	default <T> Tuple7<T,T2,T3,T4,T5,T6,T7> map1(Function<T1,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.map1(fn);
		else
			return Tuples.tuple(fn.apply(v1()),v2(),v3(),v4(),v5(),v6(),v7());
	}
	/**
	 * Lazily Map 1st element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple7<T,T2,T3,T4,T5,T6,T7> lazyMap1(Function<T1,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.lazyMap1(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(7){
			public T v1(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v1())); 
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
	default <T> Tuple7<T1,T,T3,T4,T5,T6,T7> lazyMap2(Function<T2,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.lazyMap2(fn);
		

		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return  new TupleImpl(7){
			
			public T v2(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v2())); 
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
	default <T> Tuple7<T1,T,T3,T4,T5,T6,T7> map2(Function<T2,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.map2(fn);
		return of(v1(),fn.apply(v2()),v3(),v4(),v5(),v6(),v7());
	}
	/**
	 * Lazily Map 3rd element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T> Tuple7<T1,T2,T,T4,T5,T6,T7> lazyMap3(Function<T3,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.lazyMap3(fn);

		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return new TupleImpl(7){
			
			public T v3(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v3())); 
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
	
	/* 
	 * @see com.aol.cyclops.lambda.tuple.Tuple4#map3(java.util.function.Function)
	 */
	default <T> Tuple7<T1,T2,T,T4,T5,T6,T7> map3(Function<T3,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.map3(fn);
		return of(v1(),v2(),fn.apply(v3()),v4(),v5(),v6(),v7());
	}
	/**
	 * Lazily Map 4th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple7<T1,T2,T3,T,T5,T6,T7> lazyMap4(Function<T4,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.lazyMap4(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return  new TupleImpl(7){
			
			public T v4(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v4())); 
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
	/* 
	 * Map element 4
	 * @see com.aol.cyclops.lambda.tuple.Tuple4#map4(java.util.function.Function)
	 */
	default <T> Tuple7<T1,T2,T3,T,T5,T6,T7> map4(Function<T4,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.map4(fn);
		return of(v1(),v2(),v3(),fn.apply(v4()),v5(),v6(),v7());
	}
	/*
	 * Lazily Map 5th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple7<T1,T2,T3,T4,T,T6,T7> lazyMap5(Function<T5,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.lazyMap5(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return  new TupleImpl(7){
			
			public T v5(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v5())); 
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
	 * Map the 5th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple5
	 */
	default <T> Tuple7<T1,T2,T3,T4,T,T6,T7> map5(Function<T5,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.map5(fn);
		return of(v1(),v2(),v3(),v4(),fn.apply(v5()),v6(),v7());
	}
	/*
	 * Lazily Map 6th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple7<T1,T2,T3,T4,T5,T,T7> lazyMap6(Function<T6,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.lazyMap6(fn);
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return  new TupleImpl(7){
			
			public T v6(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v6())); 
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
	 * 
	 * Map the 6th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple7
	 */
	default <T> Tuple7<T1,T2,T3,T4,T5,T,T7> map6(Function<T6,T> fn){
		if(arity()!=7)
			return (Tuple7)Tuple6.super.map6(fn);
		return of(v1(),v2(),v3(),v4(),v5(),fn.apply(v6()),v7());
	}
	/*
	 * Lazily Map 7th element and memoise the result
	 * @param fn Map function
	 * @return
	 */
	default <T> Tuple7<T1,T2,T3,T4,T5,T6,T> lazyMap7(Function<T7,T> fn){
		
		ImmutableClosedValue<T> value = new ImmutableClosedValue<>();
		return  new TupleImpl(7){
			
			public T v7(){
				return value.getOrSet(()->fn.apply(Tuple7.this.v7())); 
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
	 * 
	 * Map the 7th element in a tuple to a different value
	 * 
	 * @param fn Mapper function
	 * @return new Tuple7
	 */
	default <T> Tuple7<T1,T2,T3,T4,T5,T6,T> map7(Function<T7,T> fn){
		
		return of(v1(),v2(),v3(),v4(),v5(),v6(),fn.apply(v7()));
	}
	public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> ofTuple(Object tuple7){
		return (Tuple7)new TupleImpl(tuple7,7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> of(T1 t1, T2 t2,T3 t3,T4 t4,T5 t5,
																		T6 t6, T7 t7){
		return (Tuple7)new TupleImpl(Arrays.asList(t1,t2,t3,t4,t5,t6,t7),7);
	}
}
