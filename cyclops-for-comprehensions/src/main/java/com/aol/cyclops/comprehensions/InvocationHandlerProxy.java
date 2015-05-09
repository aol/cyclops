package com.aol.cyclops.comprehensions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Setter;

class InvocationHandlerProxy<X> implements InvocationHandler{

	private final Proxier proxier = new Proxier();
	private final Class<X> type; 
	@Setter
	private volatile ComprehensionData compData;
	@Setter
	private X proxy;
	
	public InvocationHandlerProxy(Class<X> type) {
		super();
		this.type = type;
		compData = null;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if(method.getName().equals("yield") && method.getParameterCount()==1){
			return handleYield(method,args);
		}
		
		else if(method.getName().equals("filter")&& method.getParameterCount()==1) {
			return handleFilter(method, args);
		}else if(method.getName().equals("run")&& method.getParameterCount()==1) {
			return handleConsume(method,args);
		}
		
		else if(method.getParameterCount()==0)
			return compData.$(method.getName());
		else if(method.getParameterCount()==1){
			return handleBind(method, args);
		}
		
		throw new RuntimeException("No method available for " + method.getName());
	}
	private <X> X handleYieldSupplier(Method method,ComprehensionData compData, Object[] args){
		
		
		 return (X)compData.yield((Supplier)args[0]);
	
	}
	private <X> X handleYield(Method method, Object[] args){
			proxier.release(type, proxy);
			if(args[0] instanceof Supplier)
				return handleYieldSupplier(method,compData,args);
				
		return (X)compData.yieldFunction((Function)args[0]);
	}
	
	private <X> X handleConsume(Method method, Object[] args){
		if(args[0] instanceof Runnable)
			compData.yield(()-> { ((Runnable)args[0]).run(); return null;});
		else
			compData.yieldFunction( input-> { ((Consumer)args[0]).accept(input); return null;});
		return (X)null;
	}
	
	private <X> X handleFilter(Method method, Object[] args ){

		if(args[0] instanceof Function)
			compData.filterFunction((Function)args[0]);
		else
				compData.filter((Supplier)args[0]);
		 if(method.getReturnType().isInterface() && type!=method.getReturnType()){
			 proxier.release(type, proxy);
			 return (X)proxier.newProxy(method.getReturnType(),compData);
		 }
		return (X)proxy;
	}
	
	private <X> X handleBind(Method method,Object[] args ){
		
		String name = method.getName();
		if(method.getName().indexOf('$')!=-1)
			name = method.getName().substring(method.getName().indexOf('$'));
		 compData.$(name,applyFunction(args[0]));
		 if(method.getReturnType().isInterface() && type!=method.getReturnType()){
			 proxier.release(type, proxy);
			 return (X)proxier.newProxy(method.getReturnType(),compData);
		 }
		 return (X)proxy;
	}
	
	private Object applyFunction(Object o){
		if(o instanceof Function){
			Supplier s = ()-> ((Function)o).apply(compData.getVars());
			return s;
		}
		return o;
	}
	
}
