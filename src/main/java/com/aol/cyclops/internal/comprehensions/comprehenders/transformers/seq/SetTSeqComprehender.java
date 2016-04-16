package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;



public class SetTSeqComprehender implements Comprehender<SetTSeq>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, SetTSeq apply) {
	  
		return apply.isSeqPresent() ? comp.of(apply.stream().toSetX().collect(Collectors.toCollection(MaterializedList::new)))  : comp.empty();
	}
	@Override
    public Object filter(SetTSeq t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(SetTSeq t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(SetTSeq t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public SetTSeq of(Object o) {
		return SetTSeq.of(SetX.of(o));
	}

	@Override
	public SetTSeq empty() {
		return SetTSeq.emptySet();
	}

	@Override
	public Class getTargetClass() {
		return SetTSeq.class;
	}
    @Override
    public SetTSeq fromIterator(Iterator o) {
        return SetTSeq.of(SetX.fromIterable(()->o));
    }

}
