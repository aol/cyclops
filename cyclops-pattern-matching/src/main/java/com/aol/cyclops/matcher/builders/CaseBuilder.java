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
	public static class InCaseOfBuilder<V>{
		private final Predicate<V> match;
		private final PatternMatcher matcher;
		
		public <T,R,X> Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<V,X>> thenExtract(Extractor<T,R> extractor){
			return (ActionWithReturn<R,X> a)->{
				return new TypeSafePatternMatcher<V,X>(matcher).inCaseOfThenExtract(match, a,(Extractor<V,R>)extractor);
			};
		}
		
		public <T,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>(matcher).inCaseOf(match, a);
		}
		public <T,X>  TypeSafePatternMatcher<T,X> thenConsume(Action<V> a){
			return new TypeSafePatternMatcher<T,X>(matcher).caseOf(match, a);
		}
		
	}
	@AllArgsConstructor
	public static class InCaseOfBuilderExtractor<T,R>{
		private final Extractor<T,R> extractor;
		private final PatternMatcher patternMatcher;
		
		public <V,X> Step<ActionWithReturn<V,X>,TypeSafePatternMatcher<T,X>> isTrue(Predicate<V> match){
			return (ActionWithReturn<V,X> a)-> new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOf(extractor, match, a);
		}
		public <V,X> Step<ActionWithReturn<V,X>,TypeSafePatternMatcher<T,X>> matches(Matcher<V> match){
			return (ActionWithReturn<V,X> a)-> new TypeSafePatternMatcher<T,X>(patternMatcher).inMatchOf(extractor, match, a);
		}
		
		public <V,X> TypeSafePatternMatcher<T,X> isType(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfType(extractor, a);
		}
		public <V,X> Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<T,X>>  isValue(R value){
			return (ActionWithReturn<R,X> a ) ->{
				return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfValue(value, extractor, a);
			};
		}
		
	}
	
	@AllArgsConstructor
	public static class InMatchOfBuilder<V>{
		private final Matcher<V> match;
		private final PatternMatcher patternMatcher;
		
		public <R,X> Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<V,X>> thenExtract(Extractor<V,R> extractor){
			return (ActionWithReturn<R,X> a)->{
				return new TypeSafePatternMatcher<V,X>(patternMatcher).inMatchOfThenExtract(match, a,extractor);
			};
		}
		public <T,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inMatchOf(match, a);
		}
		public <T,X>  TypeSafePatternMatcher<T,X> thenConsume(Action<V> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).matchOf(match, a);
		}
		
	}
	
	@AllArgsConstructor
	public static class InCaseOfManyStep2<R,V,T,X>{
		private final Predicate<V>[] predicates;
		private final PatternMatcher patternMatcher;
		public  TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<List<V>, X> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfMany( a,predicates);
		}
		public  TypeSafePatternMatcher<T,X> thenConsume(Action<List<V>> a){
			return new TypeSafePatternMatcher<T,X>(patternMatcher).caseOfMany( a,predicates);
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
