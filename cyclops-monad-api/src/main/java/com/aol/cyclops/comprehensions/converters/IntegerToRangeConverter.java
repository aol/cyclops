package com.aol.cyclops.comprehensions.converters;

import java.util.function.Function;
import java.util.stream.IntStream;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class IntegerToRangeConverter implements MonadicConverter<IntStream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof Integer && ((Integer)o)>=0;
	}

	@Override
	public IntStream convertToMonadicForm(Object f) {
		return IntStream.range(0, (Integer)f);
	}
	
	

}
