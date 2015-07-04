package com.aol.cyclops.javaslang.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javaslang.control.Either;
import javaslang.control.Either.LeftProjection;
import javaslang.control.Either.RightProjection;
import javaslang.control.Left;
import javaslang.control.None;
import javaslang.control.Right;

import com.aol.cyclops.lambda.api.Comprehender;

public class LeftProjectionComprehender implements Comprehender<LeftProjection>{

	public Object filter(LeftProjection t, Predicate p){
		return t.filter(x->p.test(x));
	}
	@Override
	public Object map(LeftProjection t, Function fn) {
		return t.map(x ->fn.apply(x));
	}
	
	@Override
	public Object flatMap(LeftProjection t, Function fn) {
		return t.flatMap(x->fn.apply(x));
	}

	@Override
	public LeftProjection of(Object o) {
		return new Left(o).left();
	}

	@Override
	public LeftProjection empty() {
		return new Left(null).left().filter(x->false);
	}

	@Override
	public Class getTargetClass() {
		return LeftProjection.class;
	}

	public Object resolveForCrossTypeFlatMap(Comprehender comp,LeftProjection apply){
		Optional present =  apply.toJavaOptional();
		if(present.isPresent())
			return comp.of(apply.get());
		else
			return comp.empty();
		
	}
	
}
