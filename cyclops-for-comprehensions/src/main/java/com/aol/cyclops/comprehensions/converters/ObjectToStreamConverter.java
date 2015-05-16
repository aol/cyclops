package com.aol.cyclops.comprehensions.converters;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.AsDecomposable;
import com.aol.cyclops.lambda.api.MonadicConverter;

/**
 * Convert any Object to a Stream
 * 
 * @author johnmcclean
 *
 */
public class ObjectToStreamConverter implements MonadicConverter<Seq> {

	@Override
	public boolean accept(Object o) {
		return true;
	}

	@Override
	public Seq convertToMonadicForm(Object f) {
		return Seq.seq((Iterable)AsDecomposable.asDecomposable(f).unapply());
	}

}
