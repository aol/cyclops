package com.aol.cyclops.javaslang.comprehenders;

import java.util.function.Function;
import javaslang.test.*;
import com.aol.cyclops.lambda.api.Comprehender;

public class ArbitraryComprehender implements Comprehender<Arbitrary> {

	@Override
	public Object map(Arbitrary t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	
	@Override
	public Object flatMap(Arbitrary t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public Arbitrary of(Object o) {
		return Gen.of(o).arbitrary();
	}

	@Override
	public Arbitrary empty() {
		return Gen.fail("empty not supported!").arbitrary();
	}

	@Override
	public Class getTargetClass() {
		return Arbitrary.class;
	}
	

}
