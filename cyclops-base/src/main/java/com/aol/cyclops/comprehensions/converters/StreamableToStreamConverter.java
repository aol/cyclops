package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.api.Streamable;

public class StreamableToStreamConverter implements MonadicConverter<Stream>{

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return (o instanceof Streamable);
			
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val s = (Streamable)f;
		return s.stream();
	}
	
	

}
