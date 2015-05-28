package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.AsDecomposable;
import com.aol.cyclops.lambda.api.MonadicConverter;

/**
 * Convert any Object to a Stream
 * 
 * @author johnmcclean
 *
 */
public class ObjectToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		return true;
	}
	@Override
	public  int priority(){
		return 500;
	}
	
	@Override
	public Stream convertToMonadicForm(Object f) {
		return StreamSupport.stream(((Iterable)AsDecomposable.asDecomposable(f).unapply()).spliterator(),false);
	}

}
