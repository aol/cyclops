package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.types.extensability.MonadicConverter;
import com.aol.cyclops.util.stream.Streamable;

public class LongStreamToStreamConverter implements MonadicConverter<Stream>{

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return (o instanceof IntStream);
			
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val s = (IntStream)f;
		return s.boxed();
	}
	
	

}
