package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForComprehension1<MONAD,R,R_PARAM> {

	private final boolean convertCollections;
	
	public ForComprehension1(boolean convertCollections) {
		super();
		this.convertCollections = convertCollections;
	}
	public ForComprehension1(){
		this.convertCollections=false;
	}
	
	public static void main(String[] args){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Object result =  new ForComprehension1<Optional<?>,Optional<Integer>,Integer>()
							.<Integer,Integer>foreach(c -> c.mapAs$1(one)
															.filter(()->c.$1()>2)
															.yield(()->{return f2.apply(c.$1(), 10);}));
		System.out.println(result);
	}
	
	public <T1,T2> R foreach(Function<Step1<MONAD,T1,R,R_PARAM>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			public R execute(){
				return fn.apply(new ComphrensionData<>(this));
			}
		});
	}
	static interface Step1<MONAD,T1,R,R_PARAM>{
		public  Step2<MONAD,T1,R,R_PARAM> mapAs$1(MONAD f);
		public T1 $1();
		
	}
	static interface Step2<MONAD,T1,R,R_PARAM>{
		public  Step3<MONAD,T1,R,R_PARAM> filter(Supplier<Boolean> s);
		public R yield(Supplier<R_PARAM> s);
		
	}
	static interface Step3<MONAD,T1,R,R_PARAM>{
		public R yield(Supplier<R_PARAM> s);
		
	}
	class ComphrensionData<MONAD,T1,R,R_PARAM> implements Step1<MONAD,T1,R,R_PARAM>, Step2<MONAD,T1,R,R_PARAM>,Step3<MONAD,T1,R,R_PARAM>{
		
		BaseComprehensionData data;
		
		
		public ComphrensionData(ContextualExecutor delegate) {
			super();
			data = new BaseComprehensionData(delegate,convertCollections);
		}
		
		public Step3<MONAD,T1,R,R_PARAM> filter(Supplier<Boolean> s){
			data.guardInternal(s);
			return this;
			
		}
		
		public R yield(Supplier<R_PARAM> s){
			return data.yieldInternal(s);
			
		}
		public T1 $1(){
			return data.$Internal("_1");
		
		}
		
		public Step2<MONAD,T1,R,R_PARAM> mapAs$1(MONAD f){
			data.$Internal("_1", f);
			
			return this;
		}
		
	}
}
