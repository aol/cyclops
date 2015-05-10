package com.aol.cyclops.comprehensions.converters;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class ArrayToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		if(o==null)
			return false;
		return o.getClass().isArray();
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		return Seq.of((Object[])f);
	}

}
