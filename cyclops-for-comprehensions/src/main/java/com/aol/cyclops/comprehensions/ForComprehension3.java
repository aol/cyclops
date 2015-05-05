package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.notype.LessTypingForComprehension3;

public class ForComprehension3<MONAD,R,R_PARAM> {

	private final boolean convertCollections;
	
	public ForComprehension3(boolean convertCollections) {
		super();
		this.convertCollections = convertCollections;
	}
	public ForComprehension3(){
		this.convertCollections=false;
	}
	public static void main(String[] args){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		 ForComprehension3<Optional,Optional<Integer>,Integer> optionalComprehension = new ForComprehension3<>();
		Object result =  optionalComprehension.<Integer,Integer,Integer>foreach(c -> c.flatMapAs$1(one)
																					.flatMapAs$2(empty)
																					.mapAs$3(Optional.of(c.$1()))
																					.filter(()->c.$1()>2)
																					.yield(()->{return f2.apply(c.$1(), c.$2());}));
		System.out.println(result);
	}
	public <T1,T2,T3> R foreach(Function<Step1<MONAD,T1,T2,T3,R,R_PARAM>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			public R execute(){
				return fn.apply(new ComphrensionData<>(this));
			}
		});
	}
	
	static interface Step1<MONAD,T1,T2,T3,R,R_PARAM>{
		public  Step2<MONAD,T1,T2,T3,R,R_PARAM> flatMapAs$1(MONAD f);
		public T1 $1();
		public T2 $2();
		public T3 $3();
	}
	static interface Step2<MONAD,T1,T2,T3,R,R_PARAM>{
		public  Step3<MONAD,T1,T2,T3,R,R_PARAM> flatMapAs$2(MONAD f);
		public  Step3<MONAD,T1,T2,T3,R,R_PARAM> flatMapAs$2(Supplier<MONAD> f);
		public  Step2<MONAD,T1,T2,T3,R,R_PARAM> filter(Supplier<Boolean> s);
		
		
	}
	static interface Step3<MONAD,T1,T2,T3,R,R_PARAM>{
		public  Step4<MONAD,T1,T2,T3,R,R_PARAM> mapAs$3(MONAD f);
		public  Step4<MONAD,T1,T2,T3,R,R_PARAM> mapAs$3(Supplier<MONAD> f);
		public  Step3<MONAD,T1,T2,T3,R,R_PARAM> filter(Supplier<Boolean> s);
		
		
	}
	static interface Step4<MONAD,T1,T2,T3,R,R_PARAM>{
		public  Step4<MONAD,T1,T2,T3,R,R_PARAM> filter(Supplier<Boolean> s);
		public R yield(Supplier<R_PARAM> s);
		
	}
	static interface Step5<MONAD,T1,T2,T3,R,R_PARAM>{
		public R yield(Supplier<R_PARAM> s);
	}
	
	 class ComphrensionData<MONAD,T1,T2,T3,R,R_PARAM> implements Step1<MONAD,T1,T2,T3,R,R_PARAM>, Step2<MONAD,T1,T2,T3,R,R_PARAM>, 
												Step3<MONAD,T1,T2,T3,R,R_PARAM>,Step4<MONAD,T1,T2,T3,R,R_PARAM>, Step5<MONAD,T1,T2,T3,R,R_PARAM>{
													
		BaseComprehensionData data;
		
		
		public ComphrensionData(ContextualExecutor delegate) {
			super();
			data = new BaseComprehensionData(delegate);
		}
		
		public  ComphrensionData<MONAD,T1,T2,T3,R,R_PARAM> filter(Supplier<Boolean> s){
			data.guardInternal(s);
			return this;
			
		}
		
		public R yield(Supplier<R_PARAM> s){
			return data.yieldInternal(s);
			
		}
		public T1 $1(){
			return data.$Internal("_1");
		
		}
		public T2 $2(){
			return data.$Internal("_2");
		
		}
		public T3 $3(){
			return data.$Internal("_3");
		
		}
		public  Step2<MONAD,T1,T2,T3,R,R_PARAM> flatMapAs$1(MONAD f){
			data.$Internal("_1", f);
			
			return this;
		}
		public  Step3<MONAD,T1,T2,T3,R,R_PARAM> flatMapAs$2(MONAD f){
			data.$Internal("_2", f);
			return this;
		}
		public  Step3<MONAD,T1,T2,T3,R,R_PARAM> flatMapAs$2(Supplier<MONAD> f){
			data.$Internal("_2", f);
			return this;
		}
		public  Step4<MONAD,T1,T2,T3,R,R_PARAM> mapAs$3(MONAD f){
			data.$Internal("_3", f);
			return this;
		}
		public  Step4<MONAD,T1,T2,T3,R,R_PARAM> mapAs$3(Supplier<MONAD> f){
			data.$Internal("_3", f);
			return this;
		}
	}
}
