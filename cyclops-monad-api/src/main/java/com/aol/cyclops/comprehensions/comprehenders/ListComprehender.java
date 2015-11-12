package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.pcollections.PVector;

import com.aol.cyclops.lambda.api.Comprehender;

public class ListComprehender implements Comprehender<List> {
	public Class getTargetClass(){
		return List.class;
	}
	@Override
	public Object filter(List t, Predicate p) {
		return t.stream().filter(p).collect(Collectors.toList());
	}

	@Override
	public Object map(List t, Function fn) {
		return t.stream().map(fn).collect(Collectors.toList());
	}
	public List executeflatMap(List t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public List flatMap(List t, Function fn) {
		return (List) t.stream().flatMap(fn).collect(Collectors.toList());
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Stream;
	}
	@Override
	public List empty() {
		return Arrays.asList();
	}
	@Override
	public List of(Object o) {
		return Arrays.asList(o);
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
	

	

}
