package com.aol.cyclops.matcher.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Two;

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
		return Case.of((Two) o);
	}

	@Override
	public Case of() {
		return Case.empty;
	}

}
