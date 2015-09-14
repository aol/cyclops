package com.aol.cyclops.comprehensions.converters;

import java.io.BufferedReader;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;



public class BufferedReaderToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof BufferedReader;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val reader = (BufferedReader)f;
		return reader.lines();
	}

}
