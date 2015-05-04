package com.aol.cyclops.comprehensions;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BaseComprehensionData {
	private final boolean convertIterables;
	private ContextualExecutor delegate;
	
	private ContextualExecutor currentContext;

	
	public BaseComprehensionData(ContextualExecutor delegate, boolean convertCollections) {
		
		this.delegate = delegate;
		this.convertIterables = convertCollections;
	}
	
	public <R extends BaseComprehensionData> R guardInternal(Supplier<Boolean> s){
		((Foreach)delegate.getContext()).addExpansion(new Filter("guard", new ContextualExecutor(delegate.getContext()){
			public Object execute(){
				currentContext = this;
				return s.get();
			}
		}));
		return (R)this;
	}
	
	public <R> R yieldInternal(Supplier s){
		return (R)((Foreach)delegate.getContext()).yield(new ContextualExecutor(delegate.getContext()){
			public Object execute(){
				currentContext = this;
				return s.get();
			}
		});
		
	}
	
	public <T> T $Internal(String property){
		Object delegate = currentContext.getContext();
		System.out.println(delegate);
		return (T)((Map)delegate).get(property);
	
	
	}
	public  <R extends BaseComprehensionData> R $Internal(String name, Object f){
		Expansion g = new Expansion(name,new ContextualExecutor(this){
			public Object execute(){
				currentContext = this;
				return convertToMonadicForm(unwrapSupplier(f));
			}

			private Object unwrapSupplier(Object f) {
				if(f instanceof Supplier)
					return ((Supplier)f).get();
				return f;
			}

			
		});
		
		((Foreach)delegate.getContext()).addExpansion(g);
		
		
		return (R)this;
	}
	private Object convertToMonadicForm(Object f) {
		
		if(f instanceof Collection)
			return ((Collection)f).stream();
		if(f instanceof Map)
			return ((Map)f).entrySet().stream();
		if(!convertIterables)
			return f;
		if(f instanceof Iterable)
			return Stream.of((Iterable)f);
		return f;
	}
}
