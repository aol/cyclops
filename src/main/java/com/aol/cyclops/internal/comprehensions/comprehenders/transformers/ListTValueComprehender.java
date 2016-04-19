package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;



public class ListTValueComprehender implements Comprehender<ListTValue>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, ListTValue apply) {
	  
		return apply.isListPresent() ? comp.of(apply.get()) : comp.empty();
	}
	@Override
    public Object filter(ListTValue t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(ListTValue t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(ListTValue t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public ListTValue of(Object o) {
		return ListTValue.of(ListX.of(o));
	}

	@Override
	public ListTValue empty() {
		return ListTValue.emptyOptional();
	}

	@Override
	public Class getTargetClass() {
		return ListTValue.class;
	}
    @Override
    public ListTValue fromIterator(Iterator o) {
        return ListTValue.of(ListX.fromIterable(()->o));
    }

}
