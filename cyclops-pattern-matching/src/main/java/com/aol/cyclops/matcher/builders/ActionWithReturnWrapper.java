package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.Action;
import com.aol.cyclops.matcher.TypedFunction;

public class ActionWithReturnWrapper<T,X> implements TypedFunction<T,X>{
	final static Object NO_VALUE = new Object();
	private final Action<T> action;
	public ActionWithReturnWrapper(Action<T> action){
		this.action = action;
	}
	
	public X apply(T t){
		action.accept(t);
		return (X)NO_VALUE;
	}
}