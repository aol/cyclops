package com.aol.cyclops.matcher;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.PatternMatcher.Action;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;

@AllArgsConstructor
public class TypeSafePatternMatcher<T,X> implements Function<T,X>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final PatternMatcher matcher = new PatternMatcher();

	public X apply(T t){
		return (X)match(t).get();
	}
	public  Optional<X> match(Object t) {
		return matcher.match(t);
	}
	public Stream<X> matchMany(Object t){
		return matcher.matchMany(t);
	}
	public<R> Stream<R> matchManyFromStream(Stream s){
		return matcher.matchManyFromStream(s);
	}
	
	public <R> Optional<R> matchFromStream(Stream s){
		return matcher.matchFromStream(s);
	}

	public < R,V> TypeSafePatternMatcher<T, X> caseOfType(Extractor<T, R> extractor,
			Action<V> a) {
		 matcher.caseOfType(extractor, a);
		 return this;
	}

	public < R,V> TypeSafePatternMatcher<T, X> caseOfValue(R value,
			Extractor<T, R> extractor, Action<V> a) {
		 matcher.caseOfValue(value, extractor, a);
		 return this;
	}

	public <V> TypeSafePatternMatcher<T, X> caseOfValue(V value, Action<V> a) {
		 matcher.caseOfValue(value, a);
		 return this;
	}

	public <V> TypeSafePatternMatcher<T, X> caseOfType(Action<V> a) {
		 matcher.caseOfType(a);
		 return this;
	}

	
	public <V> TypeSafePatternMatcher<T, X> caseOf(Predicate<V> match, Action<V> a) {
		matcher.caseOf(match, a);
		 return this;
	}

	public <R, V> TypeSafePatternMatcher<T, X> caseOfThenExtract(Predicate<V> match,
			Action<R> a, Extractor<T, R> extractor) {
		 matcher.caseOfThenExtract(match, a, extractor);
		 return this;
	}

	

	public <R, V> TypeSafePatternMatcher<T, X> caseOf(Extractor<T, R> extractor,
			Predicate<R> match, Action<V> a) {
		matcher.caseOf(extractor, match, a);
		 return this;
	}

	

	public <V> TypeSafePatternMatcher<T, X> inCaseOfValue(V value, ActionWithReturn<V,X> a) {
		matcher.inCaseOfValue(value, a);
		 return this;
	}

	public <V> TypeSafePatternMatcher<T, X> inCaseOfType(ActionWithReturn<V,X> a) {
		matcher.inCaseOfType(a);
		 return this;
	}

	

	public <V> TypeSafePatternMatcher<T, X> inCaseOf(Predicate<V> match,
			ActionWithReturn<V,X> a) {
		 matcher.inCaseOf(match, a);
		 return this;
	}

	public <R, V> TypeSafePatternMatcher<T, X> inCaseOfThenExtract(Predicate<T> match,
			ActionWithReturn<R,X> a, Extractor<T, R> extractor) {
		 matcher.inCaseOfThenExtract(match, a, extractor);
		 return this;
	}
	

	public <R, V> TypeSafePatternMatcher<T, X> inCaseOf(Extractor<T, R> extractor,
			Predicate<V> match, ActionWithReturn<V,X> a) {
		matcher.inCaseOf(extractor, match, a);
		 return this;
	}

	public <R, V> TypeSafePatternMatcher<T, X> inCaseOfType(Extractor<T, R> extractor,
			ActionWithReturn<V,X> a) {
		matcher.inCaseOfType(extractor, a);
		 return this;
	}

	public <R, V> TypeSafePatternMatcher<T, X> inCaseOfValue(R value,
			Extractor<T, R> extractor, ActionWithReturn<V,X> a) {
		matcher.inCaseOfValue(value, extractor, a);
		 return this;
	}

	
	
	

	
	public <R, V> TypeSafePatternMatcher<T, X> matchOf(Extractor<T, R> extractor,
			Matcher<R> match, Action<V> a) {
		matcher.matchOf(extractor, match, a);
		 return this;
	}
	

	
	public <V> TypeSafePatternMatcher<T, X> matchOf(Matcher<V> match, Action<V> a) {
		matcher.matchOf(match, a);
		 return this;
	}

	
	public <V> TypeSafePatternMatcher<T, X> inMatchOf(Matcher<V> match,
			ActionWithReturn<V,X> a) {
		matcher.inMatchOf(match, a);
		 return this;
	}
	
	public <R, V> TypeSafePatternMatcher<T, X> matchOfThenExtract(Matcher<V> match,
			Action<V> a, Extractor<T, R> extractor) {
		 matcher.matchOfThenExtract(match, a, extractor);
		 return this;
	}

	public <R> TypeSafePatternMatcher<T, X> inMatchOfThenExtract(Matcher<T> match,
			ActionWithReturn<R,X> a, Extractor<T, R> extractor) {
		matcher.inMatchOfThenExtract(match, a, extractor);
		 return this;
	}
	
	public <R, V> TypeSafePatternMatcher<T, X> inMatchOf(Extractor<T, R> extractor,
			Matcher<V> match, ActionWithReturn<V,X> a) {
		matcher.inMatchOf(extractor, match, a);
		 return this;
	}
}
