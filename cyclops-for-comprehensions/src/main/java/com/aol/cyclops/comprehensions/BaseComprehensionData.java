package com.aol.cyclops.comprehensions;

import java.util.Map;
import java.util.function.Supplier;

public final class BaseComprehensionData {
	private ContextualExecutor delegate;
	
	private ContextualExecutor currentContext;

	
	public BaseComprehensionData(ContextualExecutor delegate) {
		
		this.delegate = delegate;
	}
	
	public <R extends BaseComprehensionData> R guardInternal(Supplier<Boolean> s){
		((Comprehension)delegate.getContext()).addExpansion(new Filter("guard", new ContextualExecutor(delegate.getContext()){
			public Object execute(){
				currentContext = this;
				return s.get();
			}
		}));
		return (R)this;
	}
	
	public <R> R yieldInternal(Supplier s){
		return (R)((Comprehension)delegate.getContext()).yield(new ContextualExecutor(delegate.getContext()){
			public Object execute(){
				currentContext = this;
				return s.get();
			}
		});
		
	}
	
	public <T> T $Internal(String property){
		Object delegate = currentContext.getContext();
		return (T)((Map)delegate).get(property);
	
	
	}
	public  <R extends BaseComprehensionData> R $Internal(String name, Object f){
		Expansion g = new Expansion(name,new ContextualExecutor(this){
			public Object execute(){
				currentContext = this;
				return f;
			}
		});
		
		((Comprehension)delegate.getContext()).addExpansion(g);
		
		
		return (R)this;
	}
}
