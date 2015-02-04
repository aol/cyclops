package com.aol.simple.react.predicates;

import java.util.function.Predicate;

public class Predicates {

	public static <T> Take<T> take(int take){
		return new Take<T>(take);
	}
	public static <T> Take<T> limit(int take){
		return new Take<T>(take);
	}
	public static <T> Predicate<T> skip(int skip){
		Predicate limit = new Take<T>(skip);
		return (t) -> !(limit.test(t));
	}
	
	public static <T> Sample<T> sample(int rate){
		return new Sample<T>(rate);
	}
	
	static class Take<T> implements Predicate<T>{

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
	
	static class Sample<T> implements Predicate<T>{
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
