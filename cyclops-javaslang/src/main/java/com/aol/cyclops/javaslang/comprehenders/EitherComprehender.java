package com.aol.cyclops.javaslang.comprehenders;

import java.util.function.Function;

import javaslang.control.Either;
import javaslang.control.Left;
import javaslang.control.Right;

import com.aol.cyclops.lambda.api.Comprehender;

public class EitherComprehender implements Comprehender<Either> {

	@Override
	public Object map(Either t, Function fn) {
		return t.right().map(e->fn.apply(e));
	}

	@Override
	public Object flatMap(Either t, Function fn) {
		return t.right().flatMap(e->fn.apply(e));
	}

	@Override
	public Either of(Object o) {
		return new Right(o);
	}

	@Override
	public Either empty() {
		return new Left(null);
	}

	@Override
	public Class getTargetClass() {
		return Either.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Either apply){
		if(apply.isRight())
			return comp.of(apply.right().get());
		return comp.empty();
	}
}
