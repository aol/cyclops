package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import com.aol.cyclops.react.stream.traits.BaseSimpleReactStream;
import com.aol.cyclops.react.stream.traits.SimpleReactStream;
import com.aol.cyclops.types.extensability.Comprehender;

public class SimpleReactStreamComprehender {//implements Comprehender<SimpleReactStream> {
/**
	public static int priority = 4;
	@Override
	public int priority(){
		return priority;
	}
	@Override
	public Object filter(SimpleReactStream t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(SimpleReactStream t, Function fn) {
		return t.then(fn);
	}

	@Override
	public SimpleReactStream flatMap(SimpleReactStream t, Function fn) {
		return  SimpleReactStream.bind(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}

	@Override
	public SimpleReactStream of(Object o) {
		return (SimpleReactStream)BaseSimpleReactStream.of(o);
	}

	@Override
	public SimpleReactStream empty() {
		return (SimpleReactStream)BaseSimpleReactStream.empty();
	}

	@Override
	public Class getTargetClass() {
		return SimpleReactStream.class;
	}
	public Object resolveForCrossTypeFlatMap(Comprehender comp,SimpleReactStream apply){
		return comp.of(apply.block());
	}
	static <T> T unwrapOtherMonadTypes(Comprehender<T> comp,Object apply){
		
		
		
		if(apply instanceof Collection){
			return (T)((Collection)apply).stream();
		}
		if(apply instanceof Iterable){
			 return (T)StreamSupport.stream(((Iterable)apply).spliterator(),
						false);
		}
		
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}
**/
}
