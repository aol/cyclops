package com.aol.cyclops.internal.comprehensions.converters;

import java.io.BufferedReader;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;



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
