package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.BaseStream;

import javaslang.collection.List;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class ListComprehender implements Comprehender<List> {

	@Override
	public Object map(List t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(List t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(List t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public List of(Object o) {
		return List.of(o);
	}

	@Override
	public List empty() {
		return List.empty();
	}

	@Override
	public Class getTargetClass() {
		return List.class;
	}
	static List unwrapOtherMonadTypes(Comprehender<List> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return List.of( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return List.of( ((Iterable)apply).iterator());
		if(apply instanceof LazySeq){
			apply = List.of(((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return List.ofAll((Collection)apply);
		}
		final Object finalApply = apply;
		if(apply instanceof BaseStream){
			return List.ofAll( () -> ((BaseStream)finalApply).iterator());
					
		}
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
