package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class OptionalComprehender implements Comprehender<Optional> {

	@Override
	public Object filter(Optional o,Predicate p) {
		return o.filter(p);
	}

	@Override
	public Object map(Optional o,Function fn) {
		return o.map(fn);
	}

	@Override
	public Optional flatMap(Optional o,Function fn) {
		return o.flatMap(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Optional;
	}

	@Override
	public Optional of(Object o) {
		return Optional.of(o);
	}

}
