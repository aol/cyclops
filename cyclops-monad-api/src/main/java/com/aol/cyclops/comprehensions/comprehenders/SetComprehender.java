package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.Comprehender;

public class SetComprehender implements Comprehender<Set> {
	public Class getTargetClass(){
		return Set.class;
	}
	@Override
	public Object filter(Set t, Predicate p) {
		return t.stream().filter(p).collect(Collectors.toSet());
	}

	@Override
	public Object map(Set t, Function fn) {
		return t.stream().map(fn).collect(Collectors.toSet());
	}
	public Set executeflatMap(Set t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Set flatMap(Set t, Function fn) {
		return (Set) t.stream().flatMap(fn).collect(Collectors.toSet());
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Stream;
	}
	@Override
	public Set empty() {
		return Collections.unmodifiableSet(new HashSet());
	}
	@Override
	public Set of(Object o) {
		Set set= new HashSet();
		set.add(o);
		return Collections.unmodifiableSet(set);
	}
	static Stream unwrapOtherMonadTypes(Comprehender comp,Object apply){
		
		
		
		if(apply instanceof Collection){
			return ((Collection)apply).stream();
		}
		if(apply instanceof Iterable){
			 return StreamSupport.stream(((Iterable)apply).spliterator(),
						false);
		}
		
		Object o = Comprehender.unwrapOtherMonadTypes(comp,apply);
		
		return (Stream)o;
		
	}
	

	

}
