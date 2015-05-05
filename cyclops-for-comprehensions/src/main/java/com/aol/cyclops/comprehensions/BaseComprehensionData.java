package com.aol.cyclops.comprehensions;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BaseComprehensionData {
	
	private ContextualExecutor delegate;
	
	private ContextualExecutor currentContext;

	
	public BaseComprehensionData(ContextualExecutor delegate) {
		
		this.delegate = delegate;
		
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
				return unwrapSupplier(f);
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
	
}
