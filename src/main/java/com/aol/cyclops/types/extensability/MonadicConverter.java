package com.aol.cyclops.types.extensability;

import java.util.function.Function;

public interface MonadicConverter<T> {
	public boolean accept(Object o);
	public T convertToMonadicForm(Object f);
	default int priority(){
		return 5;
	}
	
}
