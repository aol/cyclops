package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.function.Function;

import javaslang.collection.Queue;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class QueueComprehender implements Comprehender<Queue> {

	@Override
	public Object map(Queue t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(Queue t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Queue t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public Queue of(Object o) {
		return Queue.of(o);
	}

	@Override
	public Queue empty() {
		return Queue.empty();
	}

	@Override
	public Class getTargetClass() {
		return Queue.class;
	}
	static Queue unwrapOtherMonadTypes(Comprehender<Queue> comp,Object apply){
		if(apply instanceof java.util.stream.Stream)
			return Queue.ofAll( ((java.util.stream.Stream)apply).iterator());
		if(apply instanceof Iterable)
			return Queue.ofAll( ((Iterable)apply).iterator());
		if(apply instanceof LazySeq){
			apply = Queue.ofAll(((LazySeq)apply).iterator());
		}
		if(apply instanceof Collection){
			return Queue.ofAll((Collection)apply);
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}

}
