package com.aol.cyclops.matcher;

import com.aol.cyclops.matcher.builders.IterableCase;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.StreamCase;

public class CollectionMatching {
	/**
	 * Create a builder for matching on the disaggregated elements of a collection.
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher per element
	 * 
	 * @return Iterable / Collection based Pattern Matching Builder
	 */
	public static final<USER_VALUE> IterableCase<USER_VALUE> whenIterable(){
		IterableCase cse = new IterableCase(new PatternMatcher());
		return cse;
	}
	
	/**
	 * Create a builder that builds Pattern Matching Cases from Streams of data.
	 * 
	 * 
	 * @return Stream based Pattern Matching Builder
	 */
	public static final  StreamCase whenFromStream(){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return cse;
	}
	
	
	
	
	
}
