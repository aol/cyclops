package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Getter;


public class ComprehensionData<T,R,V extends Initialisable<?>> implements Initialisable{
	private final BaseComprehensionData data;
	@Getter
	private final V vars;
	
	private final Proxier proxier = new Proxier();
	
	public ComprehensionData(ExecutionState state,Optional<Class<V>> varsClass) {
		super();
		data = new BaseComprehensionData(state);
		this.vars = varsClass.map(c->proxier.newProxy(c,this))
						.orElse((V)new Varsonly().init(data));
	}
	public ComprehensionData(V vars,ExecutionState state) {
		super();
		data = new BaseComprehensionData(state);
		this.vars = (V)vars.init(data);
	}
	ComprehensionData<T,R,V> filterSupplier(Supplier<Boolean> s){
		data.guardInternal(s);
		return this;
		
	}
	
	public  ComprehensionData<T,R,V> filter(Function<V,Boolean> s){
		data.guardInternal(()->s.apply(vars));
		return this;
		
	}
	R yieldSupplier(Supplier s){
		return data.yieldInternal(s);
		
	}
	public <R> R yield(Function<V,?> s){
		return data.yieldInternal(()->s.apply(vars));
		
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
	
	public  <T> ComprehensionData<T,R,V> $(String name,Object f){
		data.$Internal(name, f);
		
		return (ComprehensionData)this;
	}
	public  <T> ComprehensionData<T,R,V> $(String name,Supplier f){
		data.$Internal(name, f);
		
		return (ComprehensionData)this;
	}
	public   <T> ComprehensionData<T,R,V> $1(Object f){
		data.$Internal("_1", f);
		
		return (ComprehensionData)this;
	}
	public   <T> ComprehensionData<T,R,V> $2(Object f){
		data.$Internal("_2", f);
		return (ComprehensionData)this;
	}
	@Override
	public Initialisable init(BaseComprehensionData data) {
		return this;
	}
	
	
}
