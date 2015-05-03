package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;



public class ForComprehension {
	
	public static void main(String[] args){

		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result =  ForComprehension.foreach(c -> c.$1(one)
														.$2(empty)
													//	.guard(()->c.<Integer>$1()>2)
														.yield(()->{return f2.apply(c.$1(), c.$2());}));
	System.out.println(result);
}
	@SuppressWarnings("unchecked")
	public static <T,R> R foreach(Function<ComphrensionData<T,R>,R> fn){
		return (R)Comprehension.foreach(new ContextualExecutor("delegate"){
			public Object execute(){
				return fn.apply(new ComphrensionData(this));
			}
		});
	}

	static class ComphrensionData<T,R> {
		BaseComprehensionData data;
		
		
		public ComphrensionData(ContextualExecutor delegate) {
			super();
			data = new BaseComprehensionData(delegate);
		}
		
		public  ComphrensionData<T,R> guard(Supplier<Boolean> s){
			data.guardInternal(s);
			return this;
			
		}
		
		public <R> R yield(Supplier s){
			return data.yieldInternal(s);
			
		}
		public <T> T $(String name){
			return data.$Internal(name);
		
		}
		public <T> T $1(){
			return data.$Internal("_1");
		
		}
		public <T> T $2(){
			return data.$Internal("_2");
		
		}
		public <T> T $3(){
			return data.$Internal("_3");
		
		}
		public  <T> ComphrensionData<T,R> $(String name,Object f){
			data.$Internal(name, f);
			
			return (ComphrensionData)this;
		}
		public  <T> ComphrensionData<T,R> $1(Object f){
			data.$Internal("_1", f);
			
			return (ComphrensionData)this;
		}
		public  <T> ComphrensionData<T,R> $2(Object f){
			data.$Internal("_2", f);
			return (ComphrensionData)this;
		}
		public  <T> ComphrensionData<T,R> $3(Object f){
			data.$Internal("_3", f);
			return (ComphrensionData)this;
		}
	}
}
