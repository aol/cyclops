package com.aol.cyclops.internal.comprehensions.converters;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

public class InputStreamToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof InputStream;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		return new BufferedReaderToStreamConverter().convertToMonadicForm((new BufferedReader( new InputStreamReader((InputStream)f) )));
	}

}
