package com.aol.cyclops.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalLong;

import lombok.val;

public class OptionalDoubleToOptionalConverter implements
		MonadicConverter<Optional> {

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
