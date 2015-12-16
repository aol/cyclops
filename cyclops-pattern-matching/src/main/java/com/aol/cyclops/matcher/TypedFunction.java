package com.aol.cyclops.matcher;

import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.util.function.Function;

/**
 * Serializable function which allows Generic type information to be captured
 * 
 * @author johnmcclean
 *
 * @param <T> Input type
 * @param <R> Return type
 * 
 */
public interface TypedFunction<T,R> extends Function<T,R>, Serializable {
	
	/* 
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public R apply(T t);
	
	/**
	 * @return MethodType info for the lambda expression that implements this interface
	 */
	default MethodType getType(){
		return LambdaTypeExtractor.extractType(this);
	}
}