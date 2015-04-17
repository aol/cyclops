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
public class AggregatedCase<X> extends Case{
	@Getter(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	/** Match against single element - user provided elements will be aggregated into an iterable for matching **/
	public  <V> Step<V,X> isValue(V value){
		
	//	return new CaseBuilder.ValueStep<>(this, patternMatcher, value);
		//need to create classes now that X is infered from thenApply
		return (ActionWithReturn<V,X> a) -> addCase(patternMatcher.inCaseOfValue(value, a) );
	}
	
	private <T> MatchingInstance<T,X> addCase(Object o){
		return new MatchingInstance<>(this);
	}
	
	
	public  <T,R> MatchingInstance<T,R> isType(ActionWithReturn<T,R> a){
		patternMatcher.inCaseOfType(a);
		return new MatchingInstance<>(this);
		
	}
	public  <V> InCaseOfBuilder<V> isTrue(Predicate<V> match){
		return new InCaseOfBuilder<V>(match,patternMatcher,this);
	}
	
	public  <V> InMatchOfBuilder<V,X> isMatch(Matcher<V> match){
		
		return new InMatchOfBuilder<V,X>(match,patternMatcher,this);
	}
	
	
	
	public  <T,R,X> InCaseOfBuilderExtractor<T,R,X> extract( Extractor<T,R> extractor){
		return new InCaseOfBuilderExtractor<T,R,X>(extractor,patternMatcher,this);
	}

}
