package com.aol.cyclops.functionaljava.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.types.extensability.Comprehender;

import fj.data.Either;
import fj.data.Option;

public class EitherComprehender implements Comprehender<Either> {

	public Object filter(Either t, Predicate p){
		return t.right().filter(x->p.test(x));
	}
	@Override
	public Object map(Either t, Function fn) {
		return t.right().map(e->fn.apply(e));
	}

	@Override
	public Object flatMap(Either t, Function fn) {
		return t.right().bind(e->fn.apply(e));
	}

	@Override
	public Either of(Object o) {
		return Either.right(o);
	}

	@Override
	public Either empty() {
		return Either.right(Option.none());
	}

	@Override
	public Class getTargetClass() {
		return Either.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Either apply){
		if(apply.isRight())
			return comp.of(apply.right().value());
		return comp.empty();
	}
}
