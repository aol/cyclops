package com.aol.cyclops.comprehensions.converters;

import java.util.Collection;
import java.util.Map;

import org.jooq.lambda.Seq;

public class CollectionToStreamConverter implements MonadicConverter<Seq> {

	public boolean accept(Object o){
		return (o instanceof Collection) || (o instanceof Map);
	}
	@SuppressWarnings("rawtypes")
	public Seq convertToMonadicForm(Object f) {
			
			if(f instanceof Collection)
				return Seq.seq(((Collection)f).stream());
			if(f instanceof Map)
				return Seq.seq(((Map)f).entrySet().stream());
			
			return null; //should never happen
		}

}
