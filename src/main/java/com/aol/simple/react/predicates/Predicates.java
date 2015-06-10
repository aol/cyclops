package com.aol.simple.react.predicates;

import java.util.function.Predicate;

public class Predicates {
	Predicates(){}
	
	/**
	 * Predicate to perform a take /limit operation via a filter method
	 * 
	 * @param take Number of elements to take
	 * @return Take / limit
	 */
	public static <T> Take<T> take(int take){
		return new Take<T>(take);
	}
	/**
	 *  Predicate to perform a take / limit operation via a filter method
	 * 
	 * @param limit Number of elements to limit
	 * @return Take /limit
	 */
	public static <T> Take<T> limit(int limit){
		return new Take<T>(limit);
	}
	/**
	 * Predicate to perform a skip / drop operation via a filter method
	 * 
	 * @param skip Number of elements to skip / drop
	 * @return Skip / drop
	 */
	public static <T> Predicate<T> skip(int skip){
		Predicate limit = new Take<T>(skip);
		return (t) -> !(limit.test(t));
	}
	
	/**
	 * Predicate to sample elements at specified rate (use via filter method)
	 * 
	 * @param rate Sampling rate
	 * @return Sample predicate
	 */
	public static <T> Sample<T> sample(int rate){
		return new Sample<T>(rate);
	}
	
	public static class Take<T> implements Predicate<T>{

		private final int limit;
		private int count=0;
		
		public Take(int limit) {
		
			this.limit = limit;
		}
		
		@Override
		public synchronized boolean test(T o) {
			return count++<limit;
		}
		
	}
	
	public static class Sample<T> implements Predicate<T>{
		private final int rate;
		
		private int count = 0;
		
		public Sample(int rate) {
			this.rate = rate;
		}
		
		@Override
		public synchronized boolean test(T t) {
			return ++count % rate ==0;
		}
		
	}
	
}
