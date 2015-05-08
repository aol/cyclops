package com.aol.cyclops.comprehensions;

import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.val;

import com.aol.cyclops.lambda.utils.ImmutableClosedValue;

@AllArgsConstructor
public class FreeFormForComprehension<X> {
	private final State state;
	private final Class<X> c;
	
	public  FreeFormForComprehension(){
		state= new State();
		c= null;
	}
	public  FreeFormForComprehension(Class c){
		state= new State();
		this.c=c;
	}
	public  FreeFormForComprehension(State s){
		this.state= s;
		c= null;
	}
	
	@SuppressWarnings("unchecked")
	<T,R> R foreachNoClass(Function<ComphrensionData<T,R>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
				return fn.apply(new ComphrensionData(new ExecutionState(this, state)));
			}
		});
	}
	@SuppressWarnings("unchecked")
	public <R> R foreach(Function<X,R> fn){
		if(c==null)
			return (R)foreachNoClass((Function)fn);
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
			
				val compData = new ComphrensionData(new ExecutionState(this, state));
				
				X proxy = newProxy(c,compData);
				
				return fn.apply(proxy);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private static<X> X newProxy(Class<X> type, ComphrensionData compData){
		ImmutableClosedValue<X> xClosed = new ImmutableClosedValue<>();
		X proxy= (X)Proxy.newProxyInstance(FreeFormForComprehension.class
				.getClassLoader(), new Class[]{type}, (prxy,
				method, args) -> {
					if(method.getName().equals("yield") && method.getParameterCount()==1 && method.getParameterTypes()[0].isAssignableFrom(Supplier.class)){
						 if(method.getReturnType().isInterface() && type!=method.getReturnType())
							 return newProxy(method.getReturnType(),compData);
						return compData.yield((Supplier)args[0]);
					}else if(method.getName().equals("filter")&& method.getParameterCount()==1 && method.getParameterTypes()[0].isAssignableFrom(Supplier.class)) {
						compData.filter((Supplier)args[0]);
						 if(method.getReturnType().isInterface() && type!=method.getReturnType())
							 return newProxy(method.getReturnType(),compData);
						return xClosed.get();
					}
					else if(method.getParameterCount()==0)
						return compData.$(method.getName());
					else if(method.getParameterCount()==1){
						String name = method.getName();
						if(method.getName().indexOf('$')!=-1)
							name = method.getName().substring(method.getName().indexOf('$'));
						 compData.$(name,args[0]);
						 if(method.getReturnType().isInterface() && type!=method.getReturnType())
							 return newProxy(method.getReturnType(),compData);
						 return xClosed.get();
						//
					}
					
					throw new RuntimeException("No method available for " + method.getName());
		});
		xClosed.setOnce(proxy);
		return proxy;
	}
	
	public static class ComphrensionData<T,R> {
		BaseComprehensionData data;
		
		
		public ComphrensionData(ExecutionState state) {
			super();
			data = new BaseComprehensionData(state);
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
