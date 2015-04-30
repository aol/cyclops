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
 * @param <X> Return type
 * 
 */
public interface ActionWithReturn<T,X> extends Function<T,X>, Serializable {
	
	public X apply(T t);
	
	/**
	 * @return MethodType info
	 */
	default MethodType getType(){
		return LambdaTypeExtractor.extractType(this);
	}
}