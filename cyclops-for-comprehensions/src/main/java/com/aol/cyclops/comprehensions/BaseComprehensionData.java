package com.aol.cyclops.comprehensions;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.github.mperry.fg.Comprehension;
import com.github.mperry.fg.Generator;


public final class BaseComprehensionData {
	private Closure delegate;
	
	private Closure currentContext;
	private Map<String,Generator> generators = new HashMap<>();
	
	public BaseComprehensionData(Closure delegate) {
		
		this.delegate = delegate;
	}
	
	public <R extends BaseComprehensionData> R guardInternal(Supplier<Boolean> s){
		((Comprehension)delegate.getDelegate()).invokeMethod("guard", new Closure(delegate.getDelegate()){
			public Object call(){
				currentContext = this;
				return s.get();
			}
		});
		return (R)this;
	}
	
	public <R> R yieldInternal(Supplier s){
		return (R)((Comprehension)delegate.getDelegate()).yield(new Closure(delegate.getDelegate()){
			public Object call(){
				currentContext = this;
				return s.get();
			}
		});
		
	}
	
	public <T> T $Internal(String property){
		Object delegate = currentContext.getDelegate();
		if(delegate instanceof Map){
			return (T)((Map)delegate).get(property);
		}
		return (T)((GroovyObject)currentContext.getDelegate()).getProperty(property);
	
	}
	public  <R extends BaseComprehensionData> R $Internal(String name, Object f){
		Generator g = (Generator)((Comprehension)delegate.getDelegate()).getProperty(name);
		g.leftShift(new Closure(delegate){ public Object call() { return f;}});
		generators.put(name,g);
		
		return (R)this;
	}
}
