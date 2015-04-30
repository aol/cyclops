package com.aol.cyclops.matcher.builders;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.matcher.ChainOfResponsibility;

/**
 * Case Builder for building Cases from Stream data
 * 
 * @author johnmcclean
 *
 */
@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class StreamCase extends Case{
	
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	/**Select matching / passing elements from Stream **/
	
	/**
	 * Build a 'Stream of responsibility' pattern 
	 * Stream of responsibility equivalent to ChainOfResponsibility pattern
	 * ChainOfResponsibility interface includes a Predicate and an Action - each will be used to build a case 
	 * 
	 * @param stream Each member of this Stream will result in one additional Case
	 * @return Pattern Matcher Builder with additional new Cases per Stream element added.
	 */
	public  <R,V,T,X> MatchingInstance<T,X> streamOfResponsibility(Stream<? extends ChainOfResponsibility<V,X>> stream){
		return addCase(patternMatcher.selectFromChain(stream));
	}
	
	
	
	/**
	 * Build a 'Stream of responsibility' pattern 
	 * Stream of responsibility equivalent to ChainOfResponsibility pattern
	 * Tuple includes a Predicate and an Action  (Function) - each will be used to build a case 
	 * 
	 * @param stream  Each member of this Stream will result in one additional Case
	 * @return Pattern Matcher Builder with additional new Cases per Stream element added.
	 */
	public  <R,V,T,X> MatchingInstance<T,X> streamOfResponsibilityFromTuple(Stream<Tuple2<Predicate<V>,Function<V,X>>> stream){
		
		return addCase(patternMatcher.selectFrom(stream));
	}
	private <T,X> MatchingInstance<T,X> addCase(PatternMatcher o){
		return new MatchingInstance<>(this.withPatternMatcher(o));
	}
	
	
	
	
	
}
