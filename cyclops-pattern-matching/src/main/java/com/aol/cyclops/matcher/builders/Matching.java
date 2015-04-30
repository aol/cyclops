package com.aol.cyclops.matcher.builders;

import java.util.function.Function;

import com.aol.cyclops.matcher.Cases;

/**
 * Pattern Matching builder
 * 
 * @author johnmcclean
 *
 */
public class Matching {

	/**
	 * Create a Pattern Matcher Builder from supplied Cases
	 * 
	 * @param cases to match on
	 * @return Pattern Mather Builder
	 */
	public static final <T,X> MatchingInstance<T,X> of(Cases<T,X,? extends Function<T,X>> cases){
		return new MatchingInstance(new _Case(new PatternMatcher().withCases(cases)));
	}
	
	/**
	 * Create a builder for Matching on Case classes. This is the closest builder
	 * for Scala / ML style pattern matching.
	 * 
	 * Case classes can be constructed succintly in Java with Lombok or jADT
	 * e.g.
	 * <pre>
	 * @Value final class CaseClass implements Decomposable { int field1; String field2;}
	 * </pre>
	 * 
	 * Use with static imports from the Predicates class to get wildcards via '__' or ANY()
	 * And to apply nested / recursive matching via Predicates.type(  ).with (   )
	 * 
	 * Match disaggregated elements by type, value, JDK 8 Predicate or Hamcrest Matcher
	 * 
	 * @return Case Class style Pattern Matching Builder
	 */
	public static final<USER_VALUE> _Case<USER_VALUE> _case(){
		_Case cse = new  _Case(new PatternMatcher());
		return cse;
	}
	/**
	 * Create a builder for Matching against a provided Object as is (i.e. the Steps this builder provide assume you don't wish to disaggregate it and
	 * match on it's decomposed parts separately).
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher
	 * 
	 * @return Simplex Element based Pattern Matching Builder
	 */
	public static final<X> ElementCase<X> newCase(){
		ElementCase<X> cse = new ElementCase<>(new PatternMatcher());
		return cse;
	}
	
	/**
	 * Create a builder for matching on the disaggregated elements of a collection.
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher per element
	 * 
	 * @return Iterable / Collection based Pattern Matching Builder
	 */
	public static final<USER_VALUE> IterableCase<USER_VALUE> iterableCase(){
		IterableCase cse = new IterableCase(new PatternMatcher());
		return cse;
	}
	
	/**
	 * Create a builder that builds Pattern Matching Cases from Streams of data.
	 * 
	 * 
	 * @return Stream based Pattern Matching Builder
	 */
	public static final  StreamCase streamCase(){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return cse;
	}
	
	
	
	
	
	/**
	 * Create a builder for Matching on Case classes. This is the closest builder
	 * for Scala / ML style pattern matching.
	 * 
	 * Case classes can be constructed succintly in Java with Lombok or jADT
	 * e.g.
	 * <pre>
	 * @Value final class CaseClass implements Decomposable { int field1; String field2;}
	 * </pre>
	 * 
	 * Use with static imports from the Predicates class to get wildcards via '__' or ANY()
	 * And to apply nested / recursive matching via Predicates.type(  ).with (   )
	 * 
	 * Match disaggregated elements by type, value, JDK 8 Predicate or Hamcrest Matcher

	 * 
	 * @param fn Function that accepts the Case for Case classes and returns the output of that builder
	 * @return Pattern Matching Builder
	 */
	public static final<X> MatchingInstance<? extends Object,X> _case(Function<_Case<? extends Object>,MatchingInstance<? extends Object,X>> fn){
		_Case cse = new _Case(new PatternMatcher());
		return fn.apply(cse);
		
	}
	/**
     * Create a builder for Matching against a provided Object as is (i.e. the Steps this builder provide assume you don't wish to disaggregate it and
	 * match on it's decomposed parts separately).
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher 
	 * 
	 * @param fn Function that accepts a Simplex Element based Pattern Matching Builder and returns it's output
	 * @return Pattern Matching Builder
	 */
	public static final<X> MatchingInstance<? extends Object,X> newCase(Function<ElementCase<X>,MatchingInstance<? extends Object,X>>fn){
		ElementCase<X> cse = new ElementCase(new PatternMatcher());
		return fn.apply(cse);
		
	}
	
	/**
	 * Create a builder for matching on the disaggregated elements of a collection.
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher per element
	 * 
	 * @param fn a Function that accepts a Iterable / Collection based Pattern Matching Builder and returns it's output
	 * @return Pattern Matching Builder
	 */
	public static final<X> MatchingInstance<? extends Object,X> iterableCase(Function<IterableCase<? extends Object>,MatchingInstance<? extends Object,X>> fn){
		IterableCase cse = new IterableCase(new PatternMatcher());
		return fn.apply(cse);
		
	}
	
	
	/**
	 * Create a builder that builds Pattern Matching Cases from Streams of data.
	 * 
	 * @param fn a function that accepts a Stream based pattern matching builder
	 * @return Pattern Matching Builder
	 */
	public static final <T,X> MatchingInstance<T,X> streamCase(Function<Case,MatchingInstance<T,X>> fn){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return fn.apply(cse);
		
	}
	

	
	
	
}
