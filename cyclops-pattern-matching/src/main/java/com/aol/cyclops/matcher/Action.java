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
	
	public void accept(T t);
	
	default MethodType getType(){
		return LambdaTypeExtractor.extractType(this);
	}
}
