package com.aol.cyclops.comprehensions.converters;

import java.util.Collection;
import java.util.Map;

public class CollectionToStreamConverter implements MonadicConverter {

	public boolean accept(Object o){
		return (o instanceof Collection) || (o instanceof Map);
	}
	@SuppressWarnings("rawtypes")
	public Object convertToMonadicForm(Object f) {
			
			if(f instanceof Collection)
				return ((Collection)f).stream();
			if(f instanceof Map)
				return ((Map)f).entrySet().stream();
			
			return f;
		}

}
