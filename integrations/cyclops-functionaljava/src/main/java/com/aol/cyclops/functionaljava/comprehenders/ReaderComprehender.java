package com.aol.cyclops.functionaljava.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;

import fj.data.Reader;

public class ReaderComprehender implements Comprehender<Reader>{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Reader apply) {
		return comp.of(apply.getFunction());
	}

	@Override
	public Object map(Reader t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(Reader t, Function fn) {
		return t.bind(r->fn.apply(r));
	}

	@Override
	public Reader of(Object o) {
		return Reader.constant(o);
	}

	@Override
	public Reader empty() {
		return Reader.constant(null);
	}

	@Override
	public Class getTargetClass() {
		return Reader.class;
	}

}
