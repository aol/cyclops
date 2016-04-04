package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class MaybeTComprehender implements ValueComprehender<MaybeTValue> {
	public Class getTargetClass(){
		return MaybeTValue.class;
	}
	@Override
	public Object filter(MaybeTValue o,Predicate p) {
		return o.filter(p);
	}

	@Override
	public Object map(MaybeTValue o,Function fn) {
		return o.map(fn);
	}
	@Override
	public Object executeflatMap(MaybeTValue t, Function fn){
        return flatMap(t,input -> Comprehender.unwrapOtherMonadTypes(buildComprehender(t),fn.apply(input)));
    }
	private Comprehender buildComprehender( MaybeTValue t) {
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
	public MaybeTValue flatMap(MaybeTValue o,Function fn) {
		return o.flatMap(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Maybe;
	}

	@Override
	public MaybeTValue of(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public MaybeTValue empty() {
	    throw new UnsupportedOperationException();
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,MaybeTValue apply){
		if(apply.isPresent())
			return comp.of(apply.get());
		else
			return comp.empty();
	}

}
