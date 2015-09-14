package com.aol.cyclops.matcher.builders;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.AnyOf.anyOf;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.TypedFunction;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilder;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfBuilder;

/**
 * Pattern Matching builder
 * 
 * @author johnmcclean
 *
 */
public class Matching {
	
	
	/**
	 *
	 *Create a Pattern Matcher Builder from supplied Cases
	 * 
	 * @param cases to match on
	 * @return Pattern Mather Builder
	 */
	public static final <T,X> MatchingInstance<T,X> of(Cases<T,X,? extends Function<T,X>> cases){
		return new MatchingInstance(new ElementCase(new PatternMatcher().withCases(cases)));
	}
	
	
	/**
	 * Create a builder for Matching against a provided Object as is (i.e. the Steps this builder provide assume you don't wish to disaggregate it and
	 * match on it's decomposed parts separately).
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher
	 * 
	 * @return Simplex Element based Pattern Matching Builder
	 */
	public static final<X> ElementCase<X> when(){
		ElementCase<X> cse = new ElementCase<>(new PatternMatcher());
		return cse;
	}
	
	
	/**
	 * Build a Case which is triggered when the user input matches the supplied Value (via Objects.equals)
	 * 
	 * @param value
	 *            will be compared to input provided in match method
	 * @return Next step in this Case builder
	 */
	public static final <V,X> Step<V, X> whenIsValue(V value) {

		return (Step<V,X>)new ElementCase<>(new PatternMatcher()).isValue(value);

	}

	/**
	 * Create a completed Case which is triggered when matching input is of the same type (T) as the input parameter to ActionWithReturn. The
	 * ActionWithReturn will then be executed and the result returned as the match result.
	 * 
	 * @param a
	 *            Action for the new Case, Predicate for the Case will be created from the input type to the Action.
	 * @return Completed Case
	 */
	public static final <T, R> MatchingInstance<T, R> whenIsType(TypedFunction<T, R> a) {

		return new ElementCase<>(new PatternMatcher()).isType(a);

	}

	/**
	 * Build a Case which is triggered when the supplied Predicate holds
	 * 
	 * @param match
	 *            Predicate which will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	public static final <V> InCaseOfBuilder<V> whenIsTrue(Predicate<V> match) {
		return new ElementCase<>(new PatternMatcher()).isTrue(match);
	}

	/**
	 * Build a Case which is triggered when the supplied Hamcrest Matcher holds
	 * 
	 * @param match
	 *            Hamcrest Matcher that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	public static final <V,X> InMatchOfBuilder<V, X> whenIsMatch(Matcher<V> match) {

		return (InMatchOfBuilder)new ElementCase<>(new PatternMatcher()).isMatch(match);
	}

	/**
	 * Build a Case which is triggered when all of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public static final <V,X> InMatchOfBuilder<V, X> whenAllMatch(Matcher<V>... matchers) {

		return (InMatchOfBuilder)new ElementCase<>(new PatternMatcher()).allMatch(matchers);
	}

	/**
	 * Build a Case which is triggered when any of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public static final  <V,X> InMatchOfBuilder<V, X> whenAnyMatch(Matcher<V>... matchers) {

		return (InMatchOfBuilder)new ElementCase<>(new PatternMatcher()).anyMatch(matchers);
	}

	/**
	 * Build a Case which is triggered when none of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public static final <V,X> InMatchOfBuilder<V, X> whenNoneMatch(Matcher<V>... matchers) {
		return (InMatchOfBuilder)new ElementCase<>(new PatternMatcher()).noneMatch(matchers);
	}


	
	
	
}
