package com.aol.cyclops.javaslang.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javaslang.control.Either;
import javaslang.control.Either.RightProjection;


import com.aol.cyclops.lambda.api.Comprehender;

import fj.data.Option;

public class RightProjectionComprehender implements Comprehender<RightProjection>{
	@Override
	public Object filter(RightProjection t, Predicate p){
		return t.filter(x->p.test(x));
	}
	@Override
	public Object map(RightProjection t, Function fn) {
		return t.map(x ->fn.apply(x));
	}

	@Override
	public Object flatMap(RightProjection t, Function fn) {
		return t.flatMap(x->fn.apply(x));
	}

	@Override
	public RightProjection of(Object o) {
		return Either.right(o).right();
	}

	@Override
	public RightProjection empty() {
		return Either.right(Option.none()).right();
	}

	@Override
	public Class getTargetClass() {
		return RightProjection.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,RightProjection apply){
		
		Optional present =  apply.toJavaOptional();
		if(present.isPresent())
			return comp.of(apply.get());
		else
			return comp.empty();
		
	}
}
