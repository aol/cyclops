package com.aol.cyclops.javaslang.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javaslang.control.Either;
import javaslang.control.Either.LeftProjection;
import javaslang.control.Either.RightProjection;
import javaslang.control.Left;
import javaslang.control.Right;

import com.aol.cyclops.lambda.api.Comprehender;

public class RightProjectionComprehender implements Comprehender<RightProjection>{
	public Object filter(LeftProjection t, Predicate p){
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
		return new Right(o).right();
	}

	@Override
	public RightProjection empty() {
		return new Right(null).right().filter(x->false);
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
