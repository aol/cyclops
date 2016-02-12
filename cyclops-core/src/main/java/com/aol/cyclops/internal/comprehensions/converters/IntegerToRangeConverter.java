package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class IntegerToRangeConverter implements MonadicConverter<Stream<Integer>> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof Integer && ((Integer)o)>=0;
	}

	@Override
	public Stream<Integer> convertToMonadicForm(Object f) {
		return IntStream.range(0, (Integer)f).boxed();
	}
	
	

}
