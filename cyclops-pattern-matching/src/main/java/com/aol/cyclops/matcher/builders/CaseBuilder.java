package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.PatternMatcher;
import com.aol.cyclops.matcher.PatternMatcher.Action;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;
import com.aol.cyclops.matcher.TypeSafePatternMatcher;

public class CaseBuilder {
	
	@AllArgsConstructor
	public static class ValueStep<V,X> implements Step<V,X>{
		private final Case cse;
		private final PatternMatcher matcher;
		private final V value;
		public <X> MatchingInstance<V,X> thenApply(ActionWithReturn<V,X> t){
			return  addCase(matcher.inCaseOfValue(value, t));
		}
		private <T,X> MatchingInstance<T,X> addCase(Object o){
			return new MatchingInstance<>(cse);
		}
	}
	@AllArgsConstructor
	public static class InCaseOfThenExtractStep<T,X,R> implements Step<R,X>{
		private final Case cse;
		private final PatternMatcher matcher;
		private final Predicate<T> match;
		private final Extractor<T,R> extractor;
		public <X> MatchingInstance<R,X> thenApply(ActionWithReturn<R,X> t){
			return addCase(matcher.inCaseOfThenExtract(match, t,extractor));
		}
		private <T,X> MatchingInstance<T,X> addCase(Object o){
			return new MatchingInstance<>(cse);
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
		private final Case cse;
		
		public <T,R,X> Step<R,X> thenExtract(Extractor<T,R> extractor){
			return  new InCaseOfThenExtractStep(cse,matcher,match,extractor);
		}
		
		
		
		public <T,X> MatchingInstance<T,X> thenApply(ActionWithReturn<V,X> a){
			return addCase(matcher.inCaseOf(match, a));
		}
		public <T,X>  MatchingInstance<T,X> thenConsume(Action<V> a){
			return addCase(matcher.caseOf(match, a));
		}
		private <T,X> MatchingInstance<T,X> addCase(Object o){
			return new MatchingInstance<>(cse);
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
		private final Case cse;
		
		public <V> Step<V,X> isTrue(Predicate<V> match){
			return new InCaseOfStep<V>(match);
					//(ActionWithReturn<V,X> a)-> addCase(patternMatcher.inCaseOf(extractor, match, a));
		}
		@AllArgsConstructor
		public class InCaseOfStep<V> implements Step<V,X>{
			private final Predicate<V> match;
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inCaseOf(extractor, match, t));
			}
			
		}
		public <V> Step<V,X> isMatch(Matcher<V> match){
			return new InMatchOf(match);
			
		}
		@AllArgsConstructor
		public class InMatchOf<V>implements Step<V,X>{
			private final Matcher<V> match;
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inMatchOf(extractor, match, t));

			}
			
		}
		
		public <V> TypeSafePatternMatcher<T,X> isType(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfType(extractor, a);
		}
		public <V> Step<V,X>  isValue(V value){
			return new InCaseOfValueStep(value);
			
		}
		@AllArgsConstructor
		public class InCaseOfValueStep<V,X> implements Step<V,X>{
			private V value;
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inCaseOfValue(value, extractor, t));
				
			}
			
		}
		private <T,R> MatchingInstance<T,R> addCase(Object o){
			return new MatchingInstance<>(cse);
		}
		
	}
	
	@AllArgsConstructor
	public static class InMatchOfBuilder<V,X>{
		private final Matcher<V> match;
		private final PatternMatcher patternMatcher;
		private final Case cse;
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
			@Override
			public <X> MatchingInstance<V, X> thenApply(ActionWithReturn<V, X> t) {
				return addCase(patternMatcher.inMatchOfThenExtract(match, t,(Extractor)extractor));
			}
			
		}
		public <T,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inMatchOf(match, a);
		}
		public <T,X>  TypeSafePatternMatcher<T,X> thenConsume(Action<V> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).matchOf(match, a);
		}
		private <T,R> MatchingInstance<T,R> addCase(Object o){
			return new MatchingInstance<>(cse);
		}
		
	}
	
	@AllArgsConstructor
	public static class InCaseOfManyStep2<R,V,T,X>{
		private final Predicate<V>[] predicates;
		private final PatternMatcher patternMatcher;
		private final Case cse;
		public  TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<List<V>, X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfMany( a,predicates);
		}
		public  MatchingInstance<T,X> thenConsume(Action<List<V>> a){
			return addCase(patternMatcher.caseOfMany( a,predicates));
		}
		private <T,X> MatchingInstance<T,X> addCase(Object o){
			return new MatchingInstance<>(cse);
		}
	}

	@AllArgsConstructor
	public static class InMatchOfManyStep2<R,V,T,X>{
		private final Matcher<V>[] predicates;
		private final PatternMatcher patternMatcher;
		
		public  TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<List<V>, X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inMatchOfMany( a,predicates);
		}
		public  TypeSafePatternMatcher<T,X> thenConsume(Action<List<V>> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).matchOfMany( a,predicates);
		}
		
	}

	
}
