package com.aol.cyclops.javaslang.comprehenders;

import java.util.function.Function;

import javaslang.control.Option;

import com.aol.cyclops.lambda.api.Comprehender;

public class OptionComprehender implements Comprehender<Option>{

	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Option apply) {
		if(apply.isDefined())
			return comp.of(apply.get());
		return comp.empty();
	}

	@Override
	public Object map(Option t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(Option t, Function fn) {
		return t.flatMap(r->fn.apply(r));
	}

	@Override
	public Option of(Object o) {
		return Option.of(o);
	}

	@Override
	public Option empty() {
		return Option.none();
	}

	@Override
	public Class getTargetClass() {
		return Option.class;
	}

}
