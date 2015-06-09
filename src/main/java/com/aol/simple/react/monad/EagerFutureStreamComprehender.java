package com.aol.simple.react.monad;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class EagerFutureStreamComprehender implements Comprehender<EagerFutureStream> {
	public static int priority = 3;
	@Override
	public int priority(){
		return priority;
	}
	@Override
	public Object filter(EagerFutureStream t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(EagerFutureStream t, Function fn) {
		return t.map(fn);
	}

	@Override
	public EagerFutureStream flatMap(EagerFutureStream t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public EagerFutureStream of(Object o) {
		return EagerFutureStream.of(o);
	}

	@Override
	public EagerFutureStream empty() {
		return EagerFutureStream.empty();
	}

	@Override
	public Class getTargetClass() {
		return EagerFutureStream.class;
	}

}
