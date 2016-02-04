package com.aol.cyclops.matcher.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.matcher.Case;

public class CaseComprehender implements Comprehender<Case>{

	@Override
	public Object filter(Case t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(Case t, Function fn) {
		return t.map(fn);
	}

	@Override
	public Case flatMap(Case t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Case;
	}

	@Override
	public Case of(Object o) {
		return Case.of((Tuple2) o);
	}

	@Override
	public Case empty() {
		return Case.empty;
	}

	@Override
	public Class getTargetClass() {
		return Case.class;
	}

}
