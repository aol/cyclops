package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public interface LazySwap {
	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2> PTuple2<T2,T1> lazySwap(PTuple2<T1,T2> host){
		
		
		return new LazySwapPTuple2<T2,T1>(host);
		
	}

	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11,12).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2,T3> PTuple3<T3,T2,T1> lazySwap(PTuple3<T1,T2,T3> host){
		
		
		return new LazySwapPTuple3<>(host);
		
	}

	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11,12,13).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2,T3,T4> PTuple4<T4,T3,T2,T1> lazySwap(PTuple4<T1,T2,T3,T4> host){
		
		
		return new TupleImpl(Arrays.asList(),4){
			
			
			public T4 v1(){
				return host.v4();
			}

			public T3 v2(){
				return host.v3();
			}

			public T2 v3(){
				return host.v2();
			}

			public T1 v4(){
				return host.v1();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}

	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11,12,13,14).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2,T3,T4,T5> PTuple5<T5,T4,T3,T2,T1> lazySwap(PTuple5<T1,T2,T3,T4,T5> host){
		
		
		return new TupleImpl(Arrays.asList(),5){
			
			
			public T5 v1(){
				return host.v5();
			}

			public T4 v2(){
				return host.v4();
			}

			public T3 v3(){
				return host.v3();
			}

			public T2 v4(){
				return host.v2();
			}

			public T1 v5(){
				return host.v1();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4(),v5());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}

	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11,12,13,14,15).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2,T3,T4,T5,T6> PTuple6<T6,T5,T4,T3,T2,T1> lazySwap(PTuple6<T1,T2,T3,T4,T5,T6> host){
		
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T6 v1(){
				return host.v6();
			}

			public T5 v2(){
				return host.v5();
			}

			public T4 v3(){
				return host.v4();
			}

			public T3 v4(){
				return host.v3();
			}

			public T2 v5(){
				return host.v2();
			}

			public T1 v6(){
				return host.v1();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4(),v5(),v6());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}

	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11,12,13,14,15,16).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2,T3,T4,T5,T6,T7> PTuple7<T7,T6,T5,T4,T3,T2,T1> lazySwap(PTuple7<T1,T2,T3,T4,T5,T6,T7> host){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T7 v1(){
				return host.v7();
			}

			public T6 v2(){
				return host.v6();
			}

			public T5 v3(){
				return host.v5();
			}

			public T4 v4(){
				return host.v4();
			}

			public T3 v5(){
				return host.v3();
			}

			public T2 v6(){
				return host.v2();
			}

			public T1 v7(){
				return host.v1();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4(),v5(),v6(),v7());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}

	
	/**
     * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
     * {@code  
     *     lazySwap(PowerTuple.of(10,11,12,13,14,15,16,17).lazyMap1(expensiveFunction));
     *  }
     *
     */
	public static <T1,T2,T3,T4,T5,T6,T7,T8> PTuple8<T8,T7,T6,T5,T4,T3,T2,T1> lazySwap(PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> host){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T8 v1(){
				return host.v8();
			}

			public T7 v2(){
				return host.v7();
			}

			public T6 v3(){
				return host.v6();
			}

			public T5 v4(){
				return host.v5();
			}

			public T4 v5(){
				return host.v4();
			}

			public T3 v6(){
				return host.v3();
			}

			public T2 v7(){
				return host.v2();
			}

			public T1 v8(){
				return host.v1();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(v1(),v2(),v3(),v4(),v5(),v6(),v7(),v8());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



}
