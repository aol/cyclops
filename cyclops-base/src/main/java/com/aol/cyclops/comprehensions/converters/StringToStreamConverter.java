package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class StringToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		return o instanceof CharSequence;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		return (((CharSequence)f).chars().boxed()).map(i ->Character.toChars(i)[0]);
	}

}
