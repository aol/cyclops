package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class CompletableFutureTComprehender implements ValueComprehender<CompletableFutureT> {
	public Class getTargetClass(){
		return CompletableFutureT.class;
	}
	@Override
	public Object filter(CompletableFutureT o,Predicate p) {
		return o.filter(p);
	}

	@Override
	public Object map(CompletableFutureT o,Function fn) {
		return o.map(fn);
	}
	@Override
	public Object executeflatMap(CompletableFutureT t, Function fn){
        return flatMap(t,input -> Comprehender.unwrapOtherMonadTypes(buildComprehender(t),fn.apply(input)));
    }
	private Comprehender buildComprehender( CompletableFutureT t) {
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
	public CompletableFutureT flatMap(CompletableFutureT o,Function fn) {
		return o.bind(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Maybe;
	}

	@Override
	public CompletableFutureT of(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public CompletableFutureT empty() {
	    throw new UnsupportedOperationException();
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,CompletableFutureT<Object> apply){
	    
	    return Matchables.completableFutureT(apply)
	            .visit(v->resolveValueForCrossTypeFlatMap(comp,v),
	                   s->comp.of(s.toCollection(MaterializedList::new)));
		
	}
	private Object resolveValueForCrossTypeFlatMap(Comprehender comp,CompletableFutureTValue<Object> apply){
	    return comp.of(apply.get());
        
	}

}
