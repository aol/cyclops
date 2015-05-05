package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class LessTypingForComprehension2<T,R> {
	
	
	
	
	
	public static void main(String[] args){

		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result =  LessTypingForComprehension2.foreach(c -> c.mapAs$1(one)
														
														
													//	.guard(()->c.<Integer>$1()>2)
														.yield(()->{return f2.apply(c.$1(), 10);}));
	System.out.println(result);
}
	@SuppressWarnings("unchecked")
	public static <T,R> R foreach(Function<Step1<T,R>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			public R execute(){
				return fn.apply(new LessTypingForComprehension2<T,R>().getComphrensionData(this));
			}
		});
	}
	protected  Step1<T, R> getComphrensionData(ContextualExecutor<R,Foreach<R>> exec) {
		return new ComphrensionData<>(exec);
	}
	public static interface Step1<T,R>{
		public  Step2<T,R> mapAs$1(Object f);
		public <T> T $1();
	
		public <T> T $(String name);
	}
	
	
	public static interface Step2<T,R>{
		public  Step3<T,R> filter(Supplier<Boolean> s);
		public <R> R yield(Supplier s);
		
	}
	public static interface Step3<T,R>{
		public <R> R yield(Supplier s);
		
	}

	class ComphrensionData<T,R> implements Step1<T,R>, Step2<T,R>,Step3<T,R>{
		BaseComprehensionData data;
		
		
		public ComphrensionData(ContextualExecutor delegate) {
			super();
			data = new BaseComprehensionData(delegate);
		}
		
		public  ComphrensionData<T,R> filter(Supplier<Boolean> s){
			data.guardInternal(s);
			return this;
			
		}
		
		public R yield(Supplier s){
			return data.yieldInternal(s);
			
		}
		public <T> T $(String name){
			return data.$Internal(name);
		
		}
		public <T> T $1(){
			return data.$Internal("_1");
		
		}
		
		public   Step2<T,R> mapAs$1(Object f){
			data.$Internal("_1", f);
			
			return (ComphrensionData)this;
		}
		
		
		
		
	}
}
