package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.MonadicConverter;

import fj.data.Seq;

public class ArrayToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		if(o==null)
			return false;
		return o.getClass().isArray();
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		return Stream.of((Object[])f);
	}

}
