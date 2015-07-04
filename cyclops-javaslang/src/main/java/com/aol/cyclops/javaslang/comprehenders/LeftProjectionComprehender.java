package com.aol.cyclops.javaslang.comprehenders;

import java.util.Optional;
import java.util.function.Function;

import javaslang.control.Either;
import javaslang.control.Either.LeftProjection;
import javaslang.control.Either.RightProjection;
import javaslang.control.Left;
import javaslang.control.Right;

import com.aol.cyclops.lambda.api.Comprehender;

public class LeftProjectionComprehender implements Comprehender<LeftProjection>{

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
		return new Right(null).left();
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
