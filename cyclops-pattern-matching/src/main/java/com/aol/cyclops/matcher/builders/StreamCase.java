package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.matcher.ChainOfResponsibility;
import com.aol.cyclops.matcher.PatternMatcher;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;
import com.aol.cyclops.matcher.Predicates;
import com.aol.cyclops.matcher.TypeSafePatternMatcher;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilder;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilderExtractor;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfManyStep2;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfBuilder;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfManyStep2;

@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class StreamCase extends Case{
	
	@Getter(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	

	/** core api **/
	
	
	/**Select matching / passing elements from Stream **/
	
	public  <R,V,T,X> TypeSafePatternMatcher<T, X> selectMatchingFromChain(Stream<? extends ChainOfResponsibility<V,X>> stream){
		return new TypeSafePatternMatcher<T,X>(patternMatcher).selectFromChain(stream);
	}
	public  <R,V,T,X> TypeSafePatternMatcher<T, X> selectMatchingFrom(Stream<Tuple2<Predicate<V>,Function<V,X>>> stream){
		
		return new TypeSafePatternMatcher<T,X>(patternMatcher).selectFrom(stream);
	}
	
	
	
	
	
	
}
