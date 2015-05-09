package com.aol.cyclops.comprehensions.converters;

import java.util.stream.IntStream;

public class IntegerToRangeConverter implements MonadicConverter<IntStream> {

	@Override
	public boolean accept(Object o) {
		return o instanceof Integer;
	}

	@Override
	public IntStream convertToMonadicForm(Object f) {
		return IntStream.range(0, (Integer)f);
	}

}
