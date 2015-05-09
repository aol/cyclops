package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;

public class StringToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		return o instanceof String;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		return Seq.of(((String)f).toCharArray());
	}

}
