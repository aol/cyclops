package com.aol.cyclops.matcher.builders;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.AnyOf.anyOf;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilder;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilderExtractor;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfBuilder;
import com.aol.cyclops.matcher2.Extractor;
import com.aol.cyclops.matcher2.TypedFunction;

/**
 * Pattern Matcher Case Builder for matching against a single element
 * 
 * @author johnmcclean
 * @param <X>
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ElementCase<X> extends CaseBeingBuilt {
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;

	/**
	 * Build a Case which is triggered when the user input matches the supplied Value (via Objects.equals)
	 * 
	 * @param value
	 *            will be compared to input provided in match method
	 * @return Next step in this Case builder
	 */
	public <V> Step<V, X> isValue(V value) {

		return new CaseBuilder.ValueStep<>(this, patternMatcher, value);

	}

	/**
	 * Create a completed Case which is triggered when matching input is of the same type (T) as the input parameter to ActionWithReturn. The
	 * ActionWithReturn will then be executed and the result returned as the match result.
	 * 
	 * @param a
	 *            Action for the new Case, Predicate for the Case will be created from the input type to the Action.
	 * @return Completed Case
	 */
	public <T, R> MatchingInstance<T, R> isType(TypedFunction<T, R> a) {

		return new MatchingInstance<>(this.withPatternMatcher(patternMatcher.inCaseOfType(a)));

	}

	/**
	 * Build a Case which is triggered when the supplied Predicate holds
	 * 
	 * @param match
	 *            Predicate which will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	public <V> InCaseOfBuilder<V> isTrue(Predicate<V> match) {
		return new InCaseOfBuilder<V>(match, patternMatcher, this);
	}

	/**
	 * Build a Case which is triggered when the supplied Hamcrest Matcher holds
	 * 
	 * @param match
	 *            Hamcrest Matcher that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	public <V> InMatchOfBuilder<V, X> isMatch(Matcher<V> match) {

		return new InMatchOfBuilder<V, X>(match, patternMatcher, this);
	}

	/**
	 * Build a Case which is triggered when all of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public final <V> InMatchOfBuilder<V, X> allMatch(Matcher<V>... matchers) {

		return new InMatchOfBuilder<V, X>(allOf(matchers), patternMatcher, this);
	}

	/**
	 * Build a Case which is triggered when any of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public final  <V> InMatchOfBuilder<V, X> anyMatch(Matcher<V>... matchers) {

		return new InMatchOfBuilder<V, X>(anyOf(matchers), patternMatcher, this);
	}

	/**
	 * Build a Case which is triggered when none of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public final <V> InMatchOfBuilder<V, X> noneMatch(Matcher<V>... matchers) {
		return new InMatchOfBuilder<V, X>(allOf(Stream.of(matchers).map(m -> not(m)).collect(Collectors.toList())), patternMatcher, this);
	}

	/**
	 * Preprocess the user supplied matching data in some way before it meets the predicate or action for this Case
	 * 
	 * @param extractor
	 *            Extractor to preprocess input data
	 * @return Next step in the Case Builder process
	 */
	public <T, R, X> InCaseOfBuilderExtractor<T, R, X> extract(Extractor<T, R> extractor) {
		return new InCaseOfBuilderExtractor<T, R, X>(extractor, patternMatcher, this);
	}

}
