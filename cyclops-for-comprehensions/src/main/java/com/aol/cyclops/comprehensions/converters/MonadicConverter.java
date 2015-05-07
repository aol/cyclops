package com.aol.cyclops.comprehensions.converters;

public interface MonadicConverter {
	public boolean accept(Object o);
	public Object convertToMonadicForm(Object f);
}
