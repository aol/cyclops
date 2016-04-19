package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;



public class OptionalTValueComprehender implements ValueComprehender<OptionalTValue>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, OptionalTValue apply) {
	  
		return apply.isPresent() ? comp.of(apply.get()) : comp.empty();
	}
	@Override
    public Object filter(OptionalTValue t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(OptionalTValue t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(OptionalTValue t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public OptionalTValue of(Object o) {
		return OptionalTValue.of(Optional.of(o));
	}

	@Override
	public OptionalTValue empty() {
		return OptionalTValue.emptyOptional();
	}

	@Override
	public Class getTargetClass() {
		return OptionalTValue.class;
	}
	

}
