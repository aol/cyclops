package com.aol.cyclops.comprehensions.converters;

import java.util.Iterator;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class IteratorToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		return o instanceof Iterator;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		return Seq.seq((Iterator)f);
	}

}
