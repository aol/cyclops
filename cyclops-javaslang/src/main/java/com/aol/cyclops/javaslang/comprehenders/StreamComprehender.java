package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.BaseStream;

import javaslang.collection.Stream;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

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
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public Stream of(Object o) {
		return Stream.of(o);
	}

	@Override
	public Stream empty() {
		return Stream.empty();
	}

	@Override
	public Class getTargetClass() {
		return Stream.class;
	}
	static Stream unwrapOtherMonadTypes(Comprehender<Stream> comp,final Object apply){
		if(apply instanceof java.util.stream.Stream)
			return Stream.ofAll( () -> ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return Stream.ofAll(((Iterable)apply));
		if(apply instanceof LazySeq){
			return Stream.ofAll(()->((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return Stream.ofAll((Collection)apply);
		}
		if(apply instanceof BaseStream){
			return Stream.ofAll( () -> ((BaseStream)apply).iterator());
					
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
