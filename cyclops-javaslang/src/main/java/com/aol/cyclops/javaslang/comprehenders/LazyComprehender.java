package com.aol.cyclops.javaslang.comprehenders;
import java.util.function.Function;
import java.util.function.Predicate;

import javaslang.Lazy;
import javaslang.concurrent.Promise;

import com.aol.cyclops.lambda.api.Comprehender;

public class LazyComprehender implements Comprehender<Lazy> {

	public Object filter(Lazy t, Predicate p){
		return t.filter(x->p.test(x));
	}
	@Override
	public Object map(Lazy t, Function fn) {
	  return t.map(i->fn.apply(i));
     }

	@Override
	public Object flatMap(Lazy t, Function fn) {
           return t.flatMap(i->fn.apply(i));
	}

	@Override
	public Lazy of(Object o) {
	  return Lazy.of(()->o);
        }

	@Override
	public Lazy empty() {
           return Lazy.undefined();
        }

	@Override
	public Class getTargetClass() {
		return Lazy.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Lazy apply){
		return comp.of(apply.get());
	}

}
