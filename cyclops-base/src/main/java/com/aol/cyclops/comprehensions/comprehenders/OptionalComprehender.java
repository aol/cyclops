package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;

import com.aol.cyclops.lambda.api.Comprehender;

public class OptionalComprehender implements Comprehender<Optional> {
	public Class getTargetClass(){
		return Optional.class;
	}
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

	@Override
	public Optional empty() {
		return Optional.empty();
	}

}
