package com.aol.cyclops.functionaljava.comprehenders;

import java.util.function.Function;

import com.aol.cyclops.lambda.api.Comprehender;

import fj.data.Option;

public class OptionComprehender implements Comprehender<Option>{

	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Option apply) {
		if(apply.isSome())
			return comp.of(apply.some());
		return comp.empty();
	}

	@Override
	public Object map(Option t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(Option t, Function fn) {
		return t.bind(r->fn.apply(r));
	}

	@Override
	public Option of(Object o) {
		return Option.fromNull(o);
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
