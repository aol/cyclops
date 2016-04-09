package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;



public class XorTValueComprehender implements ValueComprehender<XorTValue>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, XorTValue apply) {
	    return apply.isPrimary() ? comp.of(apply.get()) : comp.empty();
	}
	@Override
    public Object filter(XorTValue t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(XorTValue t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(XorTValue t, Function fn) {
	   return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public XorTValue of(Object o) {
	    System.out.println("of " + o);
		return XorTValue.of(Xor.primary(o));
	}

	@Override
	public XorTValue empty() {
		return XorTValue.emptyOptional();
	}

	@Override
	public Class getTargetClass() {
		return XorTValue.class;
	}
	

}
