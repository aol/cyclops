package com.aol.cyclops.comprehensions.converters;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class IntStreamToStreamConverter implements MonadicConverter<Stream>{

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return (o instanceof LongStream);
			
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val s = (LongStream)f;
		return s.boxed();
	}
	
	

}
