package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.extensability.MonadicConverter;



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
