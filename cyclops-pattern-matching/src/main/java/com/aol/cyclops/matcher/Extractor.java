package com.aol.cyclops.matcher;

import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.util.function.Function;

public interface Extractor<T,R> extends Function<T,R>, Serializable {
	public R apply(T t);
	default MethodType getType(){
		return LambdaTypeExtractor.extractType(this);
	}
}