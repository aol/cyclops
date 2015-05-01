package com.aol.cyclops.lambda.utils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString @EqualsAndHashCode
public class ClosedVar<T> {

	private volatile T var;
	
	public T get(){
		return var;
	}
	
	public ClosedVar<T> set(T var){
		this.var = var;
		return this;
	}
	
}
