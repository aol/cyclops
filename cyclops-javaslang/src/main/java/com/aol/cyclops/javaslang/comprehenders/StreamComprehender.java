package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.BaseStream;

import javaslang.collection.LazyStream;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class StreamComprehender implements Comprehender<LazyStream> {

	@Override
	public Object map(LazyStream t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(LazyStream t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(LazyStream t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public LazyStream of(Object o) {
		return LazyStream.of(o);
	}

	@Override
	public LazyStream empty() {
		return LazyStream.empty();
	}

	@Override
	public Class getTargetClass() {
		return LazyStream.class;
	}
	static LazyStream unwrapOtherMonadTypes(Comprehender<LazyStream> comp,final Object apply){
		if(apply instanceof java.util.stream.Stream)
			return LazyStream.ofAll( () -> ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return LazyStream.ofAll(((Iterable)apply));
		if(apply instanceof LazySeq){
			return LazyStream.ofAll(()->((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return LazyStream.ofAll((Collection)apply);
		}
		if(apply instanceof BaseStream){
			return LazyStream.ofAll( () -> ((BaseStream)apply).iterator());
					
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
