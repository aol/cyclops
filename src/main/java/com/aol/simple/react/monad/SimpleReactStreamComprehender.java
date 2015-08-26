package com.aol.simple.react.monad;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.simple.react.stream.traits.EagerSimpleReactStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

/**
 * Cyclops Monad Comprehender for SimpleReactStreams
 * @author johnmcclean
 *
 */
public class SimpleReactStreamComprehender implements Comprehender<EagerSimpleReactStream> {
	public static int priority = 4;
	@Override
	public int priority(){
		return priority;
	}
	@Override
	public Object filter(EagerSimpleReactStream t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(EagerSimpleReactStream t, Function fn) {
		return t.then(fn);
	}

	@Override
	public EagerSimpleReactStream flatMap(EagerSimpleReactStream t, Function fn) {
		return EagerSimpleReactStream.bind(t, fn);
	}

	@Override
	public EagerSimpleReactStream of(Object o) {
		return (EagerSimpleReactStream)SimpleReactStream.of(o);
	}

	@Override
	public EagerSimpleReactStream empty() {
		return (EagerSimpleReactStream)SimpleReactStream.empty();
	}

	@Override
	public Class getTargetClass() {
		return SimpleReactStream.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,EagerSimpleReactStream apply){
		return comp.of(apply.block());
	}
}
