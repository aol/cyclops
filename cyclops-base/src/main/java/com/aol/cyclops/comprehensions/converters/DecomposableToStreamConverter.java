package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.Decomposable;
import com.aol.cyclops.lambda.api.MonadicConverter;



public class DecomposableToStreamConverter implements MonadicConverter<Stream> {

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
