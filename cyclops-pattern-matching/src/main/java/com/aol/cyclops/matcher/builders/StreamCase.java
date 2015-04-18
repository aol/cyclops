package com.aol.cyclops.matcher.builders;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.matcher.ChainOfResponsibility;
import com.aol.cyclops.matcher.PatternMatcher;

@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class StreamCase extends Case{
	
	@Getter(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	

	/** core api **/
	
	
	/**Select matching / passing elements from Stream **/
	
	public  <R,V,T,X> MatchingInstance<T,X> selectMatchingFromChain(Stream<? extends ChainOfResponsibility<V,X>> stream){
		return addCase(patternMatcher.selectFromChain(stream));
	}
	public  <R,V,T,X> MatchingInstance<T,X> selectMatchingFrom(Stream<Tuple2<Predicate<V>,Function<V,X>>> stream){
		
		return addCase(patternMatcher.selectFrom(stream));
	}
	private <T,X> MatchingInstance<T,X> addCase(Object o){
		return new MatchingInstance<>(this);
	}
	
	
	
	
	
}
