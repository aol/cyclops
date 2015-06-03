package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.Action;
import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Extractor;

public class CaseBuilder {
	
	@AllArgsConstructor
	public static class ValueStep<V,X> implements Step<V,X>{
		private final CaseBeingBuilt cse;
		private final PatternMatcher matcher;
		private final V value;
		/* 
		 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn)
		 */
		public <X> MatchingInstance<V,X> thenApply(ActionWithReturn<V,X> t){
			return  addCase(matcher.inCaseOfValue(value, t));
		}
		private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
	}
	@AllArgsConstructor
	public static class InCaseOfThenExtractStep<T,X,R> implements Step<R,X>{
		private final CaseBeingBuilt cse;
		private final PatternMatcher matcher;
		private final Predicate<T> match;
		private final Extractor<T,R> extractor;
		/* 
		 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn)
		 */
		public <X> MatchingInstance<R,X> thenApply(ActionWithReturn<R,X> t){
			return addCase(matcher.inCaseOfThenExtract(match, t,extractor));
		}
		private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
	}
	@AllArgsConstructor
	public static class InCaseOfBuilder<V>{
		// T : user input (type provided to match)
		// X : match response (thenApply)
		// R : extractor response
		// V : input for matcher / predicate
		private final Predicate<V> match;
		private final PatternMatcher matcher;
		private final CaseBeingBuilt cse;
		
		/**
		 * Post-process input supplied to matching, so input to Action will be different from input to Predicate
		 * The supplied Extractor will transform input into new value which will be supplied to the Action.
		 * 
		 * @see com.aol.cyclops.matcher.Extractors
		 * @param extractor to transform input data
		 * @return Next step in the Case Builder
		 */
		public <T,R,X> Step<R,X> thenExtract(Extractor<T,R> extractor){
			return  new InCaseOfThenExtractStep(cse,matcher,match,extractor);
		}
		
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public <T,X> MatchingInstance<T,X> thenApply(ActionWithReturn<V,X> a){
			return addCase(matcher.inCaseOf(match, a));
		}
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public <T,X>  MatchingInstance<T,X> thenConsume(Action<V> a){
			return addCase(matcher.caseOf(match, a));
		}
		private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
		
	}
	@AllArgsConstructor
	public static class InCaseOfBuilderExtractor<T,R,X>{
		//T : user input (type provided to match)
		//X : match response (thenApply)
		//R : extractor response
		//V : input for matcher / predicate
		
		private final Extractor<T,R> extractor;
		private final PatternMatcher patternMatcher;
		private final CaseBeingBuilt cse;
		
		/**
		 * Add Predicate for this Case
		 * 
		 * @param match Predicate to trigger case on
		 * @return Next Step in Case Builder
		 */
		public <V> Step<V,X> isTrue(Predicate<V> match){
			return new InCaseOfStep<V>(match);
				
		}
		@AllArgsConstructor
		public class InCaseOfStep<V> implements Step<V,X>{
			private final Predicate<V> match;
			
			/* 
			 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn)
			 */
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inCaseOf(extractor, match, t));
			}
			
		}
		/**
		 * Add a Hamcrest Matcher as a Predicate for this Case
		 * 
		 * @param match Hamcrest Matcher to trigger case
		 * @return Next Step in Case Builder
		 */
		public <V> Step<V,X> isMatch(Matcher<V> match){
			return new InMatchOf(match);
			
		}
		@AllArgsConstructor
		public class InMatchOf<V>implements Step<V,X>{
			private final Matcher<V> match;
			/* 
			 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn)
			 */
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inMatchOf(extractor, match, t));

			}
			
		}
		
		/**
		 * Construct a Case that will trigger when user input is the same type (V) as supplied ActionWithReturn
		 * If triggered the ActionWithReturn will be executed and it's result will be the match result.
		 * 
		 * @param a Action for the new Case, Predicate for the Case will be created from the input type to the Action.
		 * @return Completed Case
		 */
		public <V> MatchingInstance<T,X> isType(ActionWithReturn<V,X> a){
			return addCase(patternMatcher.inCaseOfType(extractor, a));
		}

		/**
		 * Build a Case which is triggered when the user input matches the supplied Value (via Objects.equals) 
		 * 
		 * 
		 * @param value will be compared to input provided in match method
		 * @return Next step in this Case builder
		 */
		public <V> Step<V,X>  isValue(V value){
			return new InCaseOfValueStep(value);
			
		}
		@AllArgsConstructor
		public class InCaseOfValueStep<V,X> implements Step<V,X>{
			private V value;
			/* 
			 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn)
			 */
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inCaseOfValue(value, extractor, t));
				
			}
			
		}
		private <T,R> MatchingInstance<T,R> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
		
	}
	
	@AllArgsConstructor
	public static class InMatchOfBuilder<V,X>{
		private final Matcher<V> match;
		private final PatternMatcher patternMatcher;
		private final CaseBeingBuilt cse;
				//T : user input (type provided to match)
				//X : match response (thenApply)
				//R : extractor response
				//V : input for matcher / predicate
		
		public <R,X> Step<R,X> thenExtract(Extractor<? extends V,R> extractor){
			return new InMatchOfThenExtract(extractor);
		}
		@AllArgsConstructor
		public class InMatchOfThenExtract<V,X,R> implements Step<V,X>{
			private final Extractor<? extends V,R> extractor; 
			/* 
			 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn)
			 */
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inMatchOfThenExtract(match, t,(Extractor)extractor));
			}
			private <T,R> MatchingInstance<T,R> addCase(PatternMatcher o){
				return new MatchingInstance<>(cse.withPatternMatcher(o));
			}
			
		}
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public <T,X> MatchingInstance<T,X>  thenApply(ActionWithReturn<V,X> a){
			return addCase(patternMatcher.inMatchOf(match, a));
		}
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public <T,X>  MatchingInstance<T,X> thenConsume(Action<V> a){
			return addCase(patternMatcher.matchOf(match, a));
		}
		
		private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
	}
	
	@AllArgsConstructor
	public static class InCaseOfManyStep2<V>{
		private final Predicate<V>[] predicates;
		private final PatternMatcher patternMatcher;
		private final CaseBeingBuilt cse;
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public <X> MatchingInstance<V,X> thenApply(ActionWithReturn<List<V>, X> a){
			return addCase(patternMatcher.inCaseOfMany( a,predicates));
		}
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public  <X> MatchingInstance<V,X> thenConsume(Action<List<V>> a){
			return addCase(patternMatcher.caseOfMany( a,predicates));
		}
		private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
	}

	@AllArgsConstructor
	public static class InMatchOfManyStep2<R,V,T,X>{
		private final Matcher<V>[] predicates;
		private final PatternMatcher patternMatcher;
		private final CaseBeingBuilt cse;
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public  MatchingInstance<T,X> thenApply(ActionWithReturn<List<V>, X> a){
			return addCase(patternMatcher.inMatchOfMany( a,predicates));
		}
		
		/**
		 * Create a new Case with the supplied ActionWithReturn as the action
		 * 
		 * @param a Action to be executed when the new Case is triggered
		 * @return Pattern Matcher Builder
		 */
		public  MatchingInstance<T,X> thenConsume(Action<List<V>> a){
			return addCase(patternMatcher.matchOfMany( a,predicates));
		}
		private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
			return new MatchingInstance<>(cse.withPatternMatcher(o));
		}
	}

	
}
