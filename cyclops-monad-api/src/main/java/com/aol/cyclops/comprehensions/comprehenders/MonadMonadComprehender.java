package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.internal.AsGenericMonad;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.lambda.api.Comprehender;

public class MonadMonadComprehender implements Comprehender<Monad> {

	@Override
	public Object filter(Monad t, Predicate p) {
		return t.monadFilter(p);
	}

	@Override
	public Object map(Monad t, Function fn) {
		return t.monadMap(fn);
	}

	@Override
	public Monad flatMap(Monad t, Function fn) {
		return t.monadFlatMap(fn);
	}

	@Override
	public Monad of(Object o) {
		return AsGenericMonad.asMonad(o);
	}

	@Override
	public Monad empty() {
		return AsGenericMonad.monad(Optional.empty());
	}

	@Override
	public Class getTargetClass() {
		return Monad.class;
	}

	/**
	 * Answers the question how should this type behave when returned in a flatMap function
	 * by another type? For example - Optional uses comp.of(opt.get()) when a value is present
	 * and comp.empty() when no value is present.
	 * 
	 * @param comp
	 * @param apply
	 * @return
	 */
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Monad apply){
		return comp.of(apply.unwrap());
	}


}
