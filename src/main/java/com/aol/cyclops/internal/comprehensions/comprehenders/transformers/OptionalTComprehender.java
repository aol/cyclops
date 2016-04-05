package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class OptionalTComprehender implements ValueComprehender<OptionalTValue> {
	public Class getTargetClass(){
		return OptionalTValue.class;
	}
	@Override
	public Object filter(OptionalTValue o,Predicate p) {
		return o.filter(p);
	}

	@Override
	public Object map(OptionalTValue o,Function fn) {
		return o.map(fn);
	}
	@Override
	public Object executeflatMap(OptionalTValue t, Function fn){
        return flatMap(t,input -> Comprehender.unwrapOtherMonadTypes(buildComprehender(t),fn.apply(input)));
    }
	private Comprehender buildComprehender( OptionalTValue t) {
	    Comprehender delegate = this;
        return new ValueComprehender() {

            @Override
            public Object map(Object t, Function fn) {
                return delegate.map(t, fn);
            }

            @Override
            public Object flatMap(Object t, Function fn) {
                return delegate.flatMap(t, fn);
            }

            @Override
            public Object of(Object o) {
               return t.unit(o);
               
            }


            @Override
            public Object empty() {
                return t.empty();
            }

            @Override
            public Class getTargetClass() {
               return delegate.getTargetClass();
            }
        };
    }
    @Override
	public OptionalTValue flatMap(OptionalTValue o,Function fn) {
		return o.flatMapT(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Maybe;
	}

	@Override
	public OptionalTValue of(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public OptionalTValue empty() {
	    throw new UnsupportedOperationException();
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,OptionalTValue apply){
		if(apply.isPresent())
			return comp.of(apply.get());
		else
			return comp.empty();
	}

}
