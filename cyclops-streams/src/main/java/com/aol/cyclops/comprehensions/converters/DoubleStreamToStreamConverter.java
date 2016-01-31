package com.aol.cyclops.comprehensions.converters;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class DoubleStreamToStreamConverter implements MonadicConverter<Stream>{

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return (o instanceof DoubleStream);
			
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		val s = (DoubleStream)f;
		return s.boxed();
	}
	
	

}
