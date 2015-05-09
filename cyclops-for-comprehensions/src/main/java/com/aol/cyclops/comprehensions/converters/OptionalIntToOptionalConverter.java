package com.aol.cyclops.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalInt;

import lombok.val;

public class OptionalIntToOptionalConverter implements
		MonadicConverter<Optional> {

	@Override
	public boolean accept(Object o) {
		return (o instanceof OptionalInt);
	}

	@Override
	public Optional convertToMonadicForm(Object f) {
		val optional = (OptionalInt)f;
		if(optional.isPresent())
			return Optional.of(optional.getAsInt());
		return Optional.empty();
	}

}
