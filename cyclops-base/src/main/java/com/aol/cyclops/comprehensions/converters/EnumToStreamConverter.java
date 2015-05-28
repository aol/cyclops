package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

import lombok.val;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class EnumToStreamConverter implements MonadicConverter<Stream>{

	@Override
	public boolean accept(Object o) {
		if(o==null)
			return false;
		if(o instanceof Class){
			val c = (Class)o;
			return c.isEnum();
		}
		return false;
		
	}

	@Override
	public Stream convertToMonadicForm(Object f) {
		Class c = (Class)f;
		return Stream.of(c.getEnumConstants());
	}

}
