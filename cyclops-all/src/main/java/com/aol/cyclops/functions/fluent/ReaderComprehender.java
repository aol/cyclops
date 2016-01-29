package com.aol.cyclops.functions.fluent;

import java.util.Optional;
import java.util.function.Function;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.monad.Reader;



public class ReaderComprehender implements Comprehender<Reader>{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Reader apply) {
		return comp.of(apply);
	}

	@Override
	public Object map(Reader t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(Reader t, Function fn) {
		return t.flatMap(r->fn.apply(r));
	}

	@Override
	public Reader of(Object o) {
		return FluentFunctions.of(i->o);
	}

	@Override
	public Reader empty() {
		return FluentFunctions.of(i->Optional.empty());
	}

	@Override
	public Class getTargetClass() {
		return Reader.class;
	}

}
