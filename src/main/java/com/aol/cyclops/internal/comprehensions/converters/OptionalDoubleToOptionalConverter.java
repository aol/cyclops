package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalLong;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class OptionalDoubleToOptionalConverter implements
		MonadicConverter<Optional> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return (o instanceof OptionalLong);
	}

	@Override
	public Optional convertToMonadicForm(Object f) {
		val optional = (OptionalLong)f;
		if(optional.isPresent())
			return Optional.of(optional.getAsLong());
		return Optional.empty();
	}

}
