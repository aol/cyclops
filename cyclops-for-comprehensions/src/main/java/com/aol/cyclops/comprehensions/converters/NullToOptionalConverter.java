package com.aol.cyclops.comprehensions.converters;

import java.util.Optional;

public class NullToOptionalConverter implements MonadicConverter<Optional> {

	@Override
	public boolean accept(Object o) {
		return o==null;
	}

	@Override
	public Optional convertToMonadicForm(Object f) {
		return Optional.ofNullable(f);
	}

}
