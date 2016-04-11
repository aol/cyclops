package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;
import com.aol.cyclops.util.stream.Streamable;



public class StreamableTValueComprehender implements Comprehender<StreamableTValue>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, StreamableTValue apply) {
	  
		return apply.isStreamablePresent() ? comp.of(apply.get()) : comp.empty();
	}
	@Override
    public Object filter(StreamableTValue t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(StreamableTValue t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(StreamableTValue t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public StreamableTValue of(Object o) {
		return StreamableTValue.of(Streamable.of(o));
	}

	@Override
	public StreamableTValue empty() {
		return StreamableTValue.emptyOptional();
	}

	@Override
	public Class getTargetClass() {
		return StreamableTValue.class;
	}
    @Override
    public StreamableTValue fromIterator(Iterator o) {
        return StreamableTValue.of(Streamable.fromIterable(()->o));
    }
	

}
