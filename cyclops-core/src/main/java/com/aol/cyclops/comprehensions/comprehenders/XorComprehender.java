package com.aol.cyclops.comprehensions.comprehenders;


import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.lambda.api.Comprehender;

public class XorComprehender implements Comprehender<Xor> {

	public Object filter(Xor t, Predicate p){
		return t.filter(x->p.test(x));
	}
	@Override
	public Object map(Xor t, Function fn) {
		return t.map(e->fn.apply(e));
	}

	@Override
	public Object flatMap(Xor t, Function fn) {
		return t.flatMap(e->fn.apply(e));
	}

	@Override
	public Xor of(Object o) {
		return Xor.primary(o);
	}

	@Override
	public Xor empty() {
		return  Xor.primary(null);
	}

	@Override
	public Class getTargetClass() {
		return Xor.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Xor apply){
		if(apply.isPrimary())
			return comp.of(apply.get());
		return comp.empty();
	}
}

