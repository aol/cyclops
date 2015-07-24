package com.aol.cyclops.functionaljava.comprehenders;

import java.util.Collection;
import java.util.function.Function;





import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

import fj.data.Stream;

public class StreamComprehender implements Comprehender<Stream> {

	@Override
	public Object map(Stream t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(Stream t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Stream t, Function fn) {
		return t.bind(s->fn.apply(s));
	}

	@Override
	public Stream of(Object o) {
		return Stream.single(o);
	}

	@Override
	public Stream empty() {
		return Stream.nil();
	}

	@Override
	public Class getTargetClass() {
		return Stream.class;
	}
	static Stream unwrapOtherMonadTypes(Comprehender<Stream> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return Stream.iteratorStream( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return Stream.iterableStream( ((Iterable)apply));
		if(apply instanceof LazySeq){
			return Stream.iteratorStream(((LazySeq)apply).iterator());
		}
		
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,Stream apply){
		return comp.of(apply.toCollection());
	}

}
