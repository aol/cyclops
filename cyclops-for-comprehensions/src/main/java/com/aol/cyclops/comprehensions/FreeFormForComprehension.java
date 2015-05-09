package com.aol.cyclops.comprehensions;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.val;

import com.aol.cyclops.lambda.utils.ImmutableClosedValue;


public class FreeFormForComprehension<X,V extends Initialisable<?>> {
	private final State state;
	private final Class<X> c;
	private final Optional<Class<V>> varsClass;
	private final Optional<V> varsImpl;
	
	private final Proxier proxier = new Proxier();
	
	public  FreeFormForComprehension(){
		this(new State(),null,null,null);
		
	}public  FreeFormForComprehension(Class<X> c){
		this(new State(),c,null,null);
	}
	public  FreeFormForComprehension(Class<X> c,Class<V> vars){
		this(new State(),c,vars,null);
		
	}
	public  FreeFormForComprehension(Class<X> c,V vars){
		this(new State(),c,null,vars);
		
	}
	public  FreeFormForComprehension(State s){
		this(s,null,null,null);
	}
	public  FreeFormForComprehension(State s,Class<X> c,Class<V> vars,V varsImpl){
		state= s;
		this.c=c;
		this.varsClass = Optional.ofNullable(vars);
		this.varsImpl = Optional.ofNullable(varsImpl);
	}
	
	@SuppressWarnings("unchecked")
	<T,R> R foreachNoClass(Function<ComprehensionData<T,R,V>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
				return fn.apply(new ComprehensionData(new ExecutionState(this, state),varsClass));
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
			
				val compData = varsImpl.isPresent() ? new ComprehensionData(varsImpl.get(),new ExecutionState(this, state)) 
													: new ComprehensionData(new ExecutionState(this, state),varsClass);
				
					X proxy = null;
					try
					{
						proxy = proxier.newProxy(c,compData);
					
						return fn.apply(proxy);
					}finally{
						proxier.release(c,proxy);
					}
				
			}
		});
	}
	
	
	
	
	
	
	
}
