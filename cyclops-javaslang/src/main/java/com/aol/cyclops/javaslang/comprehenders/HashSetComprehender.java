package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;

import javaslang.collection.HashSet;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class HashSetComprehender implements Comprehender<HashSet> {

	@Override
	public Object map(HashSet t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(HashSet t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(HashSet t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public HashSet of(Object o) {
		return HashSet.of(o);
	}

	@Override
	public HashSet empty() {
		return HashSet.empty();
	}

	@Override
	public Class getTargetClass() {
		return HashSet.class;
	}
	static HashSet unwrapOtherMonadTypes(Comprehender<HashSet> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return HashSet.ofAll( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return HashSet.ofAll( ((Iterable)apply).iterator());
		if(apply instanceof LazySeq){
			apply = HashSet.ofAll(((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return HashSet.ofAll((Collection)apply);
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
