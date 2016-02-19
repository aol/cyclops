package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class EnumToStreamConverter implements MonadicConverter<Stream>{

	public static int priority = 5;
	public int priority(){
		return priority;
	}
	
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
