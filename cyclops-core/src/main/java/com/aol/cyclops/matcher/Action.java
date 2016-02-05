package com.aol.cyclops.matcher;

import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

/**
 * Serializable Consumer which allows Generic type information to be captured
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Action<T> extends Consumer<T>, Serializable {
	
	/* 
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(T t);
	
	/**
	 * @return MethodType info for the Lambda expression that implements this interface
	 */
	default MethodType getType(){
		return LambdaTypeExtractor.extractType(this);
	}
}
