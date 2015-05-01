package com.aol.cyclops.matcher;

class ImmutableClosedValue<T> {
	private T var;
	
	public ImmutableClosedValue(){}
	public T get(){
		return var;
	}
	
	public T setOnce(T var){
		if(this.var==null)
			this.var = var;
		return var;
	}
}
