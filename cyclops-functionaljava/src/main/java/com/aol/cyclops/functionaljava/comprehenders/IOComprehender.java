package com.aol.cyclops.functionaljava.comprehenders;

import java.util.function.Function;

import com.aol.cyclops.lambda.api.Comprehender;

import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Option;
import fj.data.State;


public class IOComprehender implements Comprehender<IO>{
	
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, IO apply) {
		return comp.empty();
	}

	@Override
	public Object map(IO t, Function fn) {
		
		return IOFunctions.map(t,r->fn.apply(r));
	}

	@Override
	public Object flatMap(IO t, Function fn) {
		return IOFunctions.flatMap(t, r->(IO)fn.apply(r));
	}

	@Override
	public IO of(Object o) {
		return IOFunctions.unit(o);
	}

	@Override
	public IO empty() {
		return IOFunctions.unit(Option.none());
	}

	@Override
	public Class getTargetClass() {
		return IO.class;
	}

}

