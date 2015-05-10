package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.Decomposable;
import com.aol.cyclops.lambda.api.MonadicConverter;



public class DecomposableToStreamConverter implements MonadicConverter<Stream> {

	@Override
	public boolean accept(Object o) {
		if(o instanceof Decomposable)
			return true;
		return false;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		return Seq.seq(((Decomposable)f).unapply());
	}

}
