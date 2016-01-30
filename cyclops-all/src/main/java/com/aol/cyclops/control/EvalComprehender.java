package com.aol.cyclops.control;

import java.util.Optional;
import java.util.function.Function;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.monad.Reader;



public class EvalComprehender implements Comprehender<Eval>{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Eval apply) {
		return comp.of(apply);
	}

	@Override
	public Object map(Eval t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(Eval t, Function fn) {
		return t.flatMap(r->fn.apply(r));
	}

	@Override
	public Eval of(Object o) {
		return Eval.later( ()-> o);
	}

	@Override
	public Eval empty() {
		return Eval.later(()->Optional.empty());
	}

	@Override
	public Class getTargetClass() {
		return Reader.class;
	}

}
