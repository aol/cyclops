package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;



public class EvalTValueComprehender implements ValueComprehender<EvalTValue>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, EvalTValue apply) {
	  
		return apply.isValuePresent() ? comp.of(apply.get()) : comp.empty();
	}
	@Override
    public Object filter(EvalTValue t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(EvalTValue t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(EvalTValue t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public EvalTValue of(Object o) {
		return EvalTValue.of(Eval.later( ()-> o));
	}

	@Override
	public EvalTValue empty() {
		return EvalTValue.emptyMaybe();
	}

	@Override
	public Class getTargetClass() {
		return EvalTValue.class;
	}
	

}
