
package com.aol.cyclops.comprehensions;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


final class BaseComprehensionData {
	
	private ContextualExecutor delegate;
	
	private ContextualExecutor currentContext;

	
	
	
	public BaseComprehensionData(ExecutionState state) {
		
		this.delegate = state.contextualExecutor;
		
		
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
	
	public void run(Runnable r){
		yieldInternal( () -> { r.run(); return null; });
	}
	public <R> R yieldInternal(Supplier s){
		return (R)((Foreach)delegate.getContext()).yield(new ExecutionState(new ContextualExecutor(delegate.getContext()){
			public Object execute(){
				currentContext = this;
				return s.get();
			}
		}));
		
	}
	
	public <T> T $Internal(String property){
		Object delegate = currentContext.getContext();
		
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
