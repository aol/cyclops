package com.aol.cyclops.comprehensions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.ComprehensionsModule.ComprehensionData;
import com.aol.cyclops.comprehensions.ComprehensionsModule.ContextualExecutor;
import com.aol.cyclops.comprehensions.ComprehensionsModule.ExecutionState;
import com.aol.cyclops.comprehensions.ComprehensionsModule.Foreach;
import com.aol.cyclops.comprehensions.ComprehensionsModule.Initialisable;

import lombok.val;


public class FreeFormForComprehension<X,V extends Initialisable> {
	
	private final Class<X> c;
	private final Optional<Class<V>> varsClass;
	private final Optional<V> varsImpl;
	
	private final Proxier proxier = new Proxier();
	
	public  FreeFormForComprehension(){
		this(null,null,null);
		
	}
	public  FreeFormForComprehension(Class<X> c){
		this(c,null,null);
	}
	public  FreeFormForComprehension(Class<X> c,Class<V> vars){
		this(c,vars,null);
		
	}
	public  FreeFormForComprehension(Class<X> c,V vars){
		this(c,null,vars);
		
	}
	
	public  FreeFormForComprehension(Class<X> c,Class<V> vars,V varsImpl){
		
		this.c=c;
		this.varsClass = Optional.ofNullable(vars);
		this.varsImpl = Optional.ofNullable(varsImpl);
	}
	
	@SuppressWarnings("unchecked")
	<T,R> R foreachNoClass(Function<ComprehensionData<T,R,V>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
				return (R)fn.apply(new ComprehensionData(new ExecutionState(this),varsClass));
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
			
				val compData = varsImpl.isPresent() ? new ComprehensionData(varsImpl.get(),new ExecutionState(this)) 
													: new ComprehensionData(new ExecutionState(this),varsClass);
							
					try
					{
					
						return fn.apply(proxier.newProxy(c,compData,proxyStore));
					}finally{
						proxier.release(c,proxyStore.get().get(c));
						proxyStore.set(new HashMap());
					}
				
			}
		});
	}
	
	
	
	private final static ThreadLocal<Map<Class,List>> proxyStore = ThreadLocal.withInitial(()->new HashMap<>());
	
	
	
}
