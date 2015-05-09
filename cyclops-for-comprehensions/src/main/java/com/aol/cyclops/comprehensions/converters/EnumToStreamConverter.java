package com.aol.cyclops.comprehensions.converters;

import lombok.val;

import org.jooq.lambda.Seq;

public class EnumToStreamConverter implements MonadicConverter<Seq>{

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
	public Seq convertToMonadicForm(Object f) {
		Class c = (Class)f;
		return Seq.of(c.getEnumConstants());
	}

}
