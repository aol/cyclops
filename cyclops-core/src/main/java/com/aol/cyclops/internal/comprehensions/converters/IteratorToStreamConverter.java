package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class IteratorToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return o instanceof Iterator;
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator)f, Spliterator.ORDERED),false );
	}

}
