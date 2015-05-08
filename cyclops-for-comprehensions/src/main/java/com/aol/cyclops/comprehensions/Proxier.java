package com.aol.cyclops.comprehensions;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.lambda.utils.ImmutableClosedValue;


class Proxier {
	@SuppressWarnings("unchecked")
	<X> X newProxy(Class<X> type, ComprehensionData compData){
		ImmutableClosedValue<X> xClosed = new ImmutableClosedValue<>();
		X proxy= (X)Proxy.newProxyInstance(FreeFormForComprehension.class
				.getClassLoader(), new Class[]{type}, (prxy,
				method, args) -> {
					if(method.getName().equals("yield") && method.getParameterCount()==1){
						return handleYield(method,compData,args);
					}
					
					else if(method.getName().equals("filter")&& method.getParameterCount()==1) {
						return handleFilter(method,compData,type, args,xClosed);
					}else if(method.getName().equals("run")&& method.getParameterCount()==1) {
						return handleConsume(method,compData, args);
					}
					
					else if(method.getParameterCount()==0)
						return compData.$(method.getName());
					else if(method.getParameterCount()==1){
						return handleBind(method,compData,type, args,xClosed);
					}
					
					throw new RuntimeException("No method available for " + method.getName());
		});
		xClosed.setOnce(proxy);
		return proxy;
	}
	
	private <X> X handleYieldSupplier(Method method,ComprehensionData compData, Object[] args){
		
		
		 return (X)compData.yield((Supplier)args[0]);
	
	}
	private <X> X handleYield(Method method,ComprehensionData compData, Object[] args){		
			if(args[0] instanceof Supplier)
				return handleYieldSupplier(method,compData,args);
				
		return (X)compData.yieldFunction((Function)args[0]);
	}
	
	private <X> X handleConsume(Method method,ComprehensionData compData, Object[] args){
		if(args[0] instanceof Runnable)
			compData.yield(()-> { ((Runnable)args[0]).run(); return null;});
		else
			compData.yieldFunction( input-> { ((Consumer)args[0]).accept(input); return null;});
		return (X)null;
	}
	
	private <X> X handleFilter(Method method,ComprehensionData compData,Class<X> type, Object[] args,ImmutableClosedValue<X> xClosed ){
		if(args[0] instanceof Function)
			compData.filterFunction((Function)args[0]);
		else
				compData.filter((Supplier)args[0]);
		 if(method.getReturnType().isInterface() && type!=method.getReturnType())
			 return (X)newProxy(method.getReturnType(),compData);
		return xClosed.get();
	}
	
	private <X> X handleBind(Method method,ComprehensionData compData,Class<X> type, Object[] args,ImmutableClosedValue<X> xClosed ){
		String name = method.getName();
		if(method.getName().indexOf('$')!=-1)
			name = method.getName().substring(method.getName().indexOf('$'));
		 compData.$(name,applyFunction(args[0],compData));
		 if(method.getReturnType().isInterface() && type!=method.getReturnType())
			 return (X)newProxy(method.getReturnType(),compData);
		 return xClosed.get();
	}
	
	private Object applyFunction(Object o, ComprehensionData compData){
		if(o instanceof Function){
			Supplier s = ()-> ((Function)o).apply(compData.getVars());
			return s;
		}
		return o;
	}
	
}
