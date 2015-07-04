package com.aol.cyclops.javaslang.comprehenders;
import java.util.function.Function;

import javaslang.control.Try;

import com.aol.cyclops.lambda.api.Comprehender;

public class TryComprehender implements Comprehender<Try> {

	@Override
	public Object map(Try t, Function fn) {
	  return t.map(i->fn.apply(i));
     }

	@Override
	public Object flatMap(Try t, Function fn) {
           return t.flatMap(i->fn.apply(i));
	}

	@Override
	public Try of(Object o) {
	  return Try.of(()->o);
        }

	@Override
	public Try empty() {
           return Try.run(()->{});
        }

	@Override
	public Class getTargetClass() {
		return Try.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Try apply){
		return comp.of(apply.get());
	}

}
