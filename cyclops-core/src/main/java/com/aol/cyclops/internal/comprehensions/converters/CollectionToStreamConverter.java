package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class CollectionToStreamConverter implements MonadicConverter<Stream> {

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	
	private static final Map<Class,Boolean> shouldConvertCache=  new ConcurrentHashMap<>();
	public boolean accept(Object o){
		return (o instanceof Collection) || (o instanceof Map) || (o instanceof Iterable && shouldConvertCache.computeIfAbsent(o.getClass(),c->shouldConvert(c)));
	}
	@SuppressWarnings("rawtypes")
	public Stream convertToMonadicForm(Object f) {
			if(f instanceof Stream)
				return (Stream)f;
			if(f instanceof Collection)
				return (((Collection)f).stream());
			if(f instanceof Map)
				return (((Map)f).entrySet().stream());
			
			if(f instanceof Iterable){
				return StreamSupport.stream(((Iterable)f).spliterator(),false);
			}
			
			return null; //should never happen
		}
	private Boolean shouldConvert(Class c) {
		if(c.isAssignableFrom(List.class))
			return false;
		return !Stream.of(c.getMethods())
		.filter(method -> "map".equals(method.getName()))
		.filter(method -> method.getParameterCount()==1).findFirst().isPresent();
	}

}
