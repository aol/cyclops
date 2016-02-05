package com.aol.cyclops.comprehensions.converters;

import java.util.Optional;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class NullToOptionalConverter implements MonadicConverter<Optional> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o==null;
	}

	@Override
	public Optional convertToMonadicForm(Object f) {
		return Optional.ofNullable(f);
	}

}
