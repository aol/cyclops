package com.aol.cyclops.matcher.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;

public class CasesComprehender implements Comprehender<Cases> {

	@Override
	public Object filter(Cases t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(Cases t, Function fn) {
		return t.map(fn);
	}

	@Override
	public Cases flatMap(Cases t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Cases;
	}

	@Override
	public Cases of(Object o) {
		if(o instanceof Case[])
			return Cases.of((Case[])o);
		else
			return Cases.ofPStack((PStack)o);
	}

	@Override
	public Cases of() {
		return Cases.of();
	}

}
