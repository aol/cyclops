package com.aol.cyclops.comprehensions.converters;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class InputStreamToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		return o instanceof InputStream;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		return new BufferedReaderToStreamConverter().convertToMonadicForm((new BufferedReader( new InputStreamReader((InputStream)f) )));
	}

}
