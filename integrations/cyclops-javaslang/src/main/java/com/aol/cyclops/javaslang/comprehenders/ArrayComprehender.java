package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.BaseStream;

import javaslang.collection.Array;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class ArrayComprehender implements Comprehender<Array> {

	@Override
	public Object map(Array t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(Array t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Array t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public Array of(Object o) {
		return Array.of(o);
	}

	@Override
	public Array empty() {
		return Array.empty();
	}

	@Override
	public Class getTargetClass() {
		return Array.class;
	}
	static Array unwrapOtherMonadTypes(Comprehender<Array> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return Array.of( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return Array.of( ((Iterable)apply).iterator());
		if(apply instanceof LazySeq){
			apply = Array.of(((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return Array.ofAll((Collection)apply);
		}
		final Object finalApply = apply;
		if(apply instanceof BaseStream){
			return Array.ofAll( () -> ((BaseStream)finalApply).iterator());
					
		}
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
