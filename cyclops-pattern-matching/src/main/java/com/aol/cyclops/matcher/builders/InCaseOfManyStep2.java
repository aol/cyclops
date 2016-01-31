package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.Action;
import com.aol.cyclops.matcher.TypedFunction;

@AllArgsConstructor
@Deprecated
public class InCaseOfManyStep2<V>{
	private final Predicate<V>[] predicates;
	private final PatternMatcher patternMatcher;
	private final CaseBeingBuilt cse;
	
	/**
	 * Create a new Case with the supplied ActionWithReturn as the action
	 * 
	 * @param a Action to be executed when the new Case is triggered
	 * @return Pattern Matcher Builder
	 */
	public <X> CollectionMatchingInstance<V,X> thenApply(TypedFunction<List<V>, X> a){
		return addCase(patternMatcher.inCaseOfMany( a,predicates));
	}
	
	/**
	 * Create a new Case with the supplied ActionWithReturn as the action
	 * 
	 * @param a Action to be executed when the new Case is triggered
	 * @return Pattern Matcher Builder
	 */
	public  <X> CollectionMatchingInstance<V,X> thenConsume(Action<List<V>> a){
		return addCase(patternMatcher.caseOfMany( a,predicates));
	}
	private <T,X> CollectionMatchingInstance<T,X> addCase(PatternMatcher o){
		return new CollectionMatchingInstance<>(cse.withPatternMatcher(o));
	}
}

