package com.aol.simple.react.monad;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.simple.react.stream.traits.SimpleReactStream;

/**
 * Cyclops Monad Comprehender for SimpleReactStreams
 * @author johnmcclean
 *
 */
public class SimpleReactStreamComprehender implements Comprehender<SimpleReactStream> {
	public static int priority = 4;
	@Override
	public int priority(){
		return priority;
	}
	@Override
	public Object filter(SimpleReactStream t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(SimpleReactStream t, Function fn) {
		return t.then(fn);
	}

	@Override
	public SimpleReactStream flatMap(SimpleReactStream t, Function fn) {
		return SimpleReactStream.bind(t, fn);
	}

	@Override
	public SimpleReactStream of(Object o) {
		return SimpleReactStream.of(o);
	}

	@Override
	public SimpleReactStream empty() {
		return SimpleReactStream.empty();
	}

	@Override
	public Class getTargetClass() {
		return SimpleReactStream.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,SimpleReactStream apply){
		return comp.of(apply.block());
	}
}
