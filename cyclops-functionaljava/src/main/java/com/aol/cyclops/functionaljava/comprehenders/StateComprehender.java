package com.aol.cyclops.functionaljava.comprehenders;

import java.util.function.Function;

import com.aol.cyclops.lambda.api.Comprehender;

import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Option;
import fj.data.State;


public class StateComprehender implements Comprehender<State>{
	
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, State apply) {
		return apply;
	}

	@Override
	public Object map(State t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(State t, Function fn) {
		return t.flatMap(r->fn.apply(r));
	}

	@Override
	public State of(Object o) {
		return State.constant(o);
	}

	@Override
	public State empty() {
		return State.constant(Option.none());
	}

	@Override
	public Class getTargetClass() {
		return State.class;
	}

}