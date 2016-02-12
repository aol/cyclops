package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.types.Decomposable;



public class DecomposableToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		if(o instanceof Decomposable)
			return true;
		return false;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		return StreamSupport.stream(((Iterable)((Decomposable)f).unapply()).spliterator(),false);
	}

}
