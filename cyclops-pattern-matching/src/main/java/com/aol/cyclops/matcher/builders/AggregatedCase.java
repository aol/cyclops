package com.aol.cyclops.matcher.builders;

import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.PatternMatcher;
import com.aol.cyclops.matcher.TypeSafePatternMatcher;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilder;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilderExtractor;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfBuilder;

@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class AggregatedCase extends Case{
	@Getter(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	/** Match against single element - user provided elements will be aggregated into an iterable for matching **/
	public  <V,T,X> Step<ActionWithReturn<V,X>,TypeSafePatternMatcher<T,X>> isValue(V value){
		return (ActionWithReturn<V,X> a) -> new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfValue(value, a) ;
	}
	
	public  <V,T,X> TypeSafePatternMatcher<T,X> isType(ActionWithReturn<T,X> a){
		return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfType(a);
		
	}
	public  <V> InCaseOfBuilder<V> isTrue(Predicate<V> match){
		return new InCaseOfBuilder<V>(match,patternMatcher);
	}
	
	public  <V> InMatchOfBuilder<V> matches(Matcher<V> match){
		
		return new InMatchOfBuilder<V>(match,patternMatcher);
	}
	
	
	
	public  <T,R> InCaseOfBuilderExtractor<T,R> extract( Extractor<T,R> extractor){
		return new InCaseOfBuilderExtractor<T,R>(extractor,patternMatcher);
	}

}
