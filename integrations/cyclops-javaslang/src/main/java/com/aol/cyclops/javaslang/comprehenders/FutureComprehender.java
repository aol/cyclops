package com.aol.cyclops.javaslang.comprehenders;
import java.util.function.Function;
import java.util.function.Predicate;

import javaslang.concurrent.Future;
import javaslang.control.Option;

import com.aol.cyclops.lambda.api.Comprehender;

public class FutureComprehender implements Comprehender<Future> {

	public Object filter(Future t, Predicate p){
		return t.filter(x->p.test(x));
	}
	@Override
	public Object map(Future t, Function fn) {
	
	  return t.map(i->fn.apply(i));
     }

	@Override
	public Object flatMap(Future t, Function fn) {
           return t.flatMap(i->fn.apply(i));
	}

	@Override
	public Future of(Object o) {
	  return Future.of(()->o);
        }

	@Override
	public Future empty() {
           return Future.of(()->Option.none());
        }

	@Override
	public Class getTargetClass() {
		return Future.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Future apply){
		return comp.of(apply.get());
	}

}
