package com.aol.cyclops.internal.comprehensions.comprehenders.transformers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;
import com.aol.cyclops.types.mixins.Printable;



public class CompletableFutureTValueComprehender implements ValueComprehender<CompletableFutureTValue>, Printable{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, CompletableFutureTValue apply) {
	 
		return apply.isFuturePresent() ? comp.of(apply.get()) : comp.empty();
	}
	@Override
    public Object filter(CompletableFutureTValue t, Predicate p){
        return t.filter(p);
    }
	@Override
	public Object map(CompletableFutureTValue t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(CompletableFutureTValue t, Function fn) {
	   
		return t.flatMapT(r->fn.apply(r));
	}

	@Override
	public CompletableFutureTValue of(Object o) {
	    System.out.println("of " + o);
		return CompletableFutureTValue.of(CompletableFuture.completedFuture(o));
	}

	@Override
	public CompletableFutureTValue empty() {
		return CompletableFutureTValue.emptyMaybe();
	}

	@Override
	public Class getTargetClass() {
		return CompletableFutureTValue.class;
	}
	

}
