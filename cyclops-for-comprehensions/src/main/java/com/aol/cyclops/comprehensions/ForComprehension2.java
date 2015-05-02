package com.aol.cyclops.comprehensions;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.mperry.fg.Comprehension;
import com.github.mperry.fg.Generator;

public class ForComprehension2<MONAD,R,R_PARAM> {

	public static void main(String[] args){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Object result =  new ForComprehension2<Optional,Optional<Integer>,Integer>()
							.<Integer,Integer>foreach(c -> c.$1(one)
															.$2(empty)
															.guard(()->c.$1()>2)
															.yield(()->{return f2.apply(c.$1(), c.$2());}));
		System.out.println(result);
	}
	public <T1,T2> R foreach(Function<ComphrensionData<MONAD,T1,T2,R,R_PARAM>,R> fn){
		return (R)Comprehension.foreach(new Closure("hello"){
			public Object call(){
				return fn.apply(new ComphrensionData(this));
			}
		});
	}
	
	static class ComphrensionData<MONAD,T1,T2,R,R_PARAM> {
		BaseComprehensionData data;
		
		
		public ComphrensionData(Closure delegate) {
			super();
			data = new BaseComprehensionData(delegate);
		}
		
		public  ComphrensionData<MONAD,T1,T2,R,R_PARAM> guard(Supplier<Boolean> s){
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
		public  ComphrensionData<MONAD,T1,T2,R,R_PARAM> $1(MONAD f){
			data.$Internal("_1", f);
			
			return this;
		}
		public  ComphrensionData<MONAD,T1,T2,R,R_PARAM> $2(MONAD f){
			data.$Internal("_2", f);
			return this;
		}
	}
}
