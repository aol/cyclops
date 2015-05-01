package com.aol.cyclops.matcher;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class ClosedVar<T> {

	private T var;
	
	public T get(){
		return var;
	}
	
	public void set(T var){
		this.var = var;
	}
	
}
