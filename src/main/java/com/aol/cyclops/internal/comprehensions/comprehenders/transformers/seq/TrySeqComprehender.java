package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.seq.TryTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;



public class TrySeqComprehender implements Comprehender<TryTSeq>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, TryTSeq apply) {
	  
		return apply.isSeqPresent() ? comp.of(apply.stream().toListX()) : comp.empty();
	}
	@Override
    public Object filter(TryTSeq t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(TryTSeq t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(TryTSeq t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public TryTSeq of(Object o) {
		return TryTSeq.of(Try.success(o));
	}

	@Override
	public TryTSeq empty() {
		return TryTSeq.emptyList();
	}

	@Override
	public Class getTargetClass() {
		return ListTSeq.class;
	}
    @Override
    public TryTSeq fromIterator(Iterator o) {
        return TryTSeq.of(Try.fromIterable(()->o));
    }

}
