package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;

import javaslang.collection.CharSeq;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class CharSeqComprehender implements Comprehender<CharSeq> {

	@Override
	public Object map(CharSeq t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(CharSeq t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(CharSeq t, Function fn) {
		return t.flatMap(s->(Iterable)fn.apply(s));
	}

	@Override
	public CharSeq of(Object o) {
		return CharSeq.of((Character)o);
	}

	@Override
	public CharSeq empty() {
		return CharSeq.empty();
	}

	@Override
	public Class getTargetClass() {
		return CharSeq.class;
	}
	static CharSeq unwrapOtherMonadTypes(Comprehender<CharSeq> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return CharSeq.ofAll( (Iterable<? extends Character>) ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return CharSeq.ofAll( (Iterable<? extends Character>) ((Iterable)apply).iterator());
		if(apply instanceof LazySeq){
			apply = CharSeq.ofAll((Iterable<? extends Character>) ((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return CharSeq.ofAll((Collection)apply);
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
