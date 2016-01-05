package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.BaseStream;

import javaslang.collection.Stack;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class StackComprehender implements Comprehender<Stack> {

	@Override
	public Object map(Stack t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(Stack t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Stack t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public Stack of(Object o) {
		return Stack.of(o);
	}

	@Override
	public Stack empty() {
		return Stack.empty();
	}

	@Override
	public Class getTargetClass() {
		return Stack.class;
	}
	static Stack unwrapOtherMonadTypes(Comprehender<Stack> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return Stack.ofAll( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return Stack.ofAll( ((Iterable)apply).iterator());
		if(apply instanceof LazySeq){
			apply = Stack.ofAll(((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return Stack.ofAll((Collection)apply);
		}
		final Object finalApply = apply;
		if(apply instanceof BaseStream){
			return Stack.ofAll( () -> ((BaseStream)finalApply).iterator());
					
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
