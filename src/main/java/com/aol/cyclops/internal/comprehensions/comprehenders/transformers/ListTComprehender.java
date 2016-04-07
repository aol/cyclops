package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

public class ListTComprehender implements ValueComprehender<ListT> {
	public Class getTargetClass(){
		return ListT.class;
	}
	@Override
	public Object filter(ListT o,Predicate p) {
		return o.filter(p);
	}

	@Override
	public Object map(ListT o,Function fn) {
		return o.map(fn);
	}
	@Override
	public Object executeflatMap(ListT t, Function fn){
        return flatMap(t,input -> Comprehender.unwrapOtherMonadTypes(buildComprehender(t),fn.apply(input)));
    }
	private Comprehender buildComprehender( ListT t) {
	    Comprehender delegate = this;
        return new Comprehender() {

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

            @Override
            public Object fromIterator(Iterator o) {
               return t.unitIterator(o);
            }
        };
    }
    @Override
	public ListT flatMap(ListT o,Function fn) {
		return o.bind(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Maybe;
	}

	@Override
	public ListT of(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public ListT empty() {
	    throw new UnsupportedOperationException();
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,ListT<Object> apply){
	    
	    return Matchables.listT(apply)
	            .visit(v->resolveValueForCrossTypeFlatMap(comp,v),
	                   s->comp.of(s.toCollection(MaterializedList::new)));
		
	}
	private Object resolveValueForCrossTypeFlatMap(Comprehender comp,ListTValue<Object> apply){
	    
	    return apply.toCollection(MaterializedList::new);
	
	}

}
