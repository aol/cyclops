package com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.Printable;



public class FutureWTSeqComprehender implements Comprehender<FutureWTSeq>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, FutureWTSeq apply) {
	  
		return apply.isSeqPresent() ? comp.of(apply.stream().toListX()) : comp.empty();
	}
	@Override
    public Object filter(FutureWTSeq t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(FutureWTSeq t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(FutureWTSeq t, Function fn) {
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public FutureWTSeq of(Object o) {
		return FutureWTSeq.of(FutureW.ofResult(o));
	}

	@Override
	public FutureWTSeq empty() {
		return FutureWTSeq.emptyList();
	}

	@Override
	public Class getTargetClass() {
		return ListTSeq.class;
	}
    @Override
    public FutureWTSeq fromIterator(Iterator o) {
        return FutureWTSeq.of(FutureW.fromIterable(()->o));
    }

}
