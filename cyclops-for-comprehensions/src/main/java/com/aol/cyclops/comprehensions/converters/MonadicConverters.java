package com.aol.cyclops.comprehensions.converters;

import lombok.Getter;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

public class MonadicConverters {
	@Getter
	private static volatile PStack<MonadicConverter> converters= ConsPStack.singleton(new CollectionToStreamConverter());
	
	public void registerConverter(MonadicConverter converter){
		converters = converters.plus(converter);
	}
	
	public static Object convertToMonadicForm(Object o){
		return converters.stream().filter(t-> t.accept(o)).map(m -> m.convertToMonadicForm(o)).findFirst().orElse(o);
	}
}
