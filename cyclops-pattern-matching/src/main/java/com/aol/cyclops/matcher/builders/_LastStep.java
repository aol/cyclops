package com.aol.cyclops.matcher.builders;

import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.TypedFunction;

@AllArgsConstructor
public  class _LastStep<R,V,T> {
	
	private final Class<T> clazz;
	private final Predicate predicate;
	private Predicate<V>[] predicates;
	private final PatternMatcher patternMatcher;
	
	public final CheckValues<T,R> then(TypedFunction<T,R> fn){
		return new _Simpler_Case(patternMatcher.inCaseOfManyType(predicate, fn,
				predicates)).withType(clazz);
	}

}
