package com.aol.cyclops.matcher.builders;

import java.util.List;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.Action;
import com.aol.cyclops.matcher.TypedFunction;
@AllArgsConstructor
public class InMatchOfManyStep2 <R,V,T,X>{
		private final Matcher<V>[] predicates;
		private final PatternMatcher patternMatcher;
		private final CaseBeingBuilt cse;
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public  CollectionMatchingInstance<T,X> thenApply(TypedFunction<List<V>, X> a){
			return addCase(patternMatcher.inMatchOfMany( a,predicates));
		}
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public  CollectionMatchingInstance<T,X> thenConsume(Action<List<V>> a){
			return addCase(patternMatcher.matchOfMany( a,predicates));
		}
		private <T,X> CollectionMatchingInstance<T,X> addCase(PatternMatcher o){
			return new CollectionMatchingInstance<>(cse.withPatternMatcher(o));
		}
	
}
