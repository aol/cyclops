package com.aol.simple.react.monad;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.val;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class LazyFutureStreamComprehender implements Comprehender<LazyFutureStream> {
	public static int priority = 3;
	@Override
	public int priority(){
		return priority;
	}
	@Override
	public Object filter(LazyFutureStream t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(LazyFutureStream t, Function fn) {
		return t.map(fn);
	}

	@Override
	public LazyFutureStream flatMap(LazyFutureStream t, Function fn) {
		return 	t.flatMap(fn);
		
		
	}

	@Override
	public LazyFutureStream of(Object o) {
		return LazyFutureStream.of(o);
	}

	@Override
	public LazyFutureStream empty() {
		return LazyFutureStream.empty();
	}

	@Override
	public Class getTargetClass() {
		return LazyFutureStream.class;
	}

}
