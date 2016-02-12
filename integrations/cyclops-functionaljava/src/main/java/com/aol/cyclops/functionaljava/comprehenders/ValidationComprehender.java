package com.aol.cyclops.functionaljava.comprehenders;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.types.extensability.Comprehender;

import jdk.nashorn.internal.runtime.regexp.joni.Option;
import fj.data.Validation;

public class ValidationComprehender implements Comprehender<Validation> {

	
	public Object filter(Validation t, Predicate p){
		return t.filter(x->p.test(x));
	}
	@Override
	public Object map(Validation t, Function fn) {
	  return t.map(i->fn.apply(i));
     }

	@Override
	public Object flatMap(Validation t, Function fn) {
           return t.bind(i->fn.apply(i));
	}

	@Override
	public Validation of(Object o) {
	  return Validation.success(o);
        }

	@Override
	public Validation empty() {
           return Validation.success(Option.NONE);
        }

	@Override
	public Class getTargetClass() {
		return Validation.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Validation apply){
		if(apply.isSuccess())
			return comp.of(apply.success());
		else
			return comp.empty();
	}

}
