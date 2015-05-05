package com.aol.cyclops.comprehensions;

import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.val;

public class FreeFormForComprehension<T,R> {
	@SuppressWarnings("unchecked")
	public static <T,R> R foreach(Function<ComphrensionData<T,R>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
				return fn.apply(new ComphrensionData(this));
			}
		});
	}
	@SuppressWarnings("unchecked")
	public static <X,R> R foreach(Class<X> c,Function<X,R> fn){
		
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
				val compData = new ComphrensionData(this);
				X proxy = (X)Proxy.newProxyInstance(FreeFormForComprehension.class
						.getClassLoader(), new Class[]{c}, (prxy,
						method, args) -> {
							if(method.getName().equals("yield") && method.getParameterCount()==1 && method.getParameterTypes()[0].isAssignableFrom(Supplier.class))
								return compData.yield((Supplier)args[0]);
							if(method.getName().equals("filter")&& method.getParameterCount()==1 && method.getParameterTypes()[0].isAssignableFrom(Supplier.class)) 
								return compData.filter((Supplier)args[0]);
							if(method.getParameterCount()==0)
								return compData.$(method.getName());
							else if(method.getParameterCount()==1)
								return compData.$(method.getName(),args[0]);
							
							throw new RuntimeException("No method available for " + method.getName());
				});
				
				return fn.apply(proxy);
			}
		});
	}
	
	static class ComphrensionData<T,R> {
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
		public <T> T $2(){
			return data.$Internal("_2");
		
		}
		
		public  <T> ComphrensionData<T,R> $(String name,Object f){
			data.$Internal(name, f);
			
			return (ComphrensionData)this;
		}
		public  <T> ComphrensionData<T,R> $(String name,Supplier f){
			data.$Internal(name, f);
			
			return (ComphrensionData)this;
		}
		public   <T> ComphrensionData<T,R> $1(Object f){
			data.$Internal("_1", f);
			
			return (ComphrensionData)this;
		}
		public   <T> ComphrensionData<T,R> $2(Object f){
			data.$Internal("_2", f);
			return (ComphrensionData)this;
		}
		
		
	}
}
