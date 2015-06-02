package com.aol.cyclops.comprehensions.converters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

	private static final Map<Class,Boolean> shouldConvertCache=  new ConcurrentHashMap<>();
	public static int priority = 500;
	@Override
	public int priority(){
		return priority;
	}
	@Override
	public boolean accept(Object o) {
		return shouldConvertCache.computeIfAbsent(o.getClass(),c->shouldConvert(c));
	}
	
	
	@Override
	public Stream convertToMonadicForm(Object f) {
		return StreamSupport.stream(((Iterable)AsDecomposable.asDecomposable(f).unapply()).spliterator(),false);
	}

	private Boolean shouldConvert(Class c) {
		return !Stream.of(c.getMethods())
		.filter(method -> "map".equals(method.getName()))
		.filter(method -> method.getParameterCount()==1).findFirst().isPresent();
	}
}
