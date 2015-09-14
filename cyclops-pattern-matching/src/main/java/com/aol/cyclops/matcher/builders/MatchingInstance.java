package com.aol.cyclops.matcher.builders;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.AnyOf.anyOf;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.Extractor;
import com.aol.cyclops.matcher.TypedFunction;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilder;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfBuilderExtractor;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfBuilder;
import com.aol.cyclops.objects.Decomposable;
/**
 * 
 * Pattern Matching builder instance
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <X>
 */
@AllArgsConstructor
public class MatchingInstance <T, X> implements Function<T, Optional<X>> {
	
	
	private final CaseBeingBuilt cse;
	
	
	
	public final Cases<T,X,TypedFunction<T,X>> cases(){
		return this.cse.getPatternMatcher().getCases();
	}
	
	/**
	 * Create a builder for Matching against a provided Object as is (i.e. the Steps this builder provide assume you don't wish to disaggregate it and
	 * match on it's decomposed parts separately).
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher
	 * 
	 * @return Simplex Element based Pattern Matching Builder
	 */
	public final ElementCase<X> when(){
		ElementCase<X> cse = new ElementCase<X>(this.cse.getPatternMatcher());
		return cse;
	}
	
	/**
	 * Build a Case which is triggered when the user input matches the supplied Value (via Objects.equals)
	 * 
	 * @param value
	 *            will be compared to input provided in match method
	 * @return Next step in this Case builder
	 */
	public <V> Step<V, X> whenIsValue(V value) {

		return (Step<V,X>)new ElementCase<>(this.cse.getPatternMatcher()).isValue(value);

	}

	/**
	 * Create a completed Case which is triggered when matching input is of the same type (T) as the input parameter to ActionWithReturn. The
	 * ActionWithReturn will then be executed and the result returned as the match result.
	 * 
	 * @param a
	 *            Action for the new Case, Predicate for the Case will be created from the input type to the Action.
	 * @return Completed Case
	 */
	public <T, R> MatchingInstance<T, R> whenIsType(TypedFunction<T, R> a) {

		return new ElementCase<>(this.cse.getPatternMatcher()).isType(a);

	}

	/**
	 * Build a Case which is triggered when the supplied Predicate holds
	 * 
	 * @param match
	 *            Predicate which will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	public <V> InCaseOfBuilder<V> whenIsTrue(Predicate<V> match) {
		return new ElementCase<>(this.cse.getPatternMatcher()).isTrue(match);
	}

	/**
	 * Build a Case which is triggered when the supplied Hamcrest Matcher holds
	 * 
	 * @param match
	 *            Hamcrest Matcher that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	public <V> InMatchOfBuilder<V, X> whenIsMatch(Matcher<V> match) {

		return (InMatchOfBuilder)new ElementCase<>(this.cse.getPatternMatcher()).isMatch(match);
	}

	/**
	 * Build a Case which is triggered when all of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public final <V> InMatchOfBuilder<V, X> whenAllMatch(Matcher<V>... matchers) {

		return (InMatchOfBuilder)new ElementCase<>(this.cse.getPatternMatcher()).allMatch(matchers);
	}

	/**
	 * Build a Case which is triggered when any of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public final  <V> InMatchOfBuilder<V, X> whenAnyMatch(Matcher<V>... matchers) {

		return (InMatchOfBuilder)new ElementCase<>(this.cse.getPatternMatcher()).anyMatch(matchers);
	}

	/**
	 * Build a Case which is triggered when none of the supplied Hamcrest Matchers holds
	 * 
	 * @param matchers
	 *            Hamcrest Matchers that will trigger this case
	 * @return Next Step in the Case Builder process
	 */
	@SafeVarargs
	public final <V> InMatchOfBuilder<V, X> whenNoneMatch(Matcher<V>... matchers) {
		return (InMatchOfBuilder)new ElementCase<>(this.cse.getPatternMatcher()).noneMatch(matchers);
	}

	
	
	
	
	/**
	 * @return Pattern Matcher as function that will return the 'unwrapped' result when apply is called.
	 *  i.e. Optional#get will be called.
	 * 
	 */
	public Function<T,X> asUnwrappedFunction(){
		return cse.getPatternMatcher().asUnwrappedFunction();
	}
	
	/**
	 * @return Pattern Matcher as a function that will return a Stream of results
	 */
	public Function<T,Stream<X>> asStreamFunction(){
		
		return	cse.getPatternMatcher().asStreamFunction();
	}
	/* 
	 *	@param t Object to match against
	 *	@return Value from matched case if present
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public Optional<X> apply(Object t){
		return (Optional<X>)cse.getPatternMatcher().apply(t);
	}
	
	/**
	 * Each input element can generated multiple matched values
	 * 
	 * @param s  Stream of data to match against (input to matcher)
	 * @return Stream of values from matched cases
	 */
	public<R> Stream<R> matchManyFromStream(Stream s){
		return cse.getPatternMatcher().matchManyFromStream(s);
	}
	
	/**
	 * 
	 * @param t input to match against - can generate multiple values
	 * @return Stream of values from matched cases for the input
	 */
	public<R> Stream<R> matchMany(Object t) {
		return cse.getPatternMatcher().matchMany(t);
	}
	
	/**
	 * Each input element can generated a single matched value
	 * 
	 * @param s Stream of data to match against (input to matcher)
	 * @return Stream of matched values, one case per input value can match
	 */
	public <R> Stream<R> matchFromStream(Stream s){
		
		return cse.getPatternMatcher().matchFromStream(s);
	}
	/**
	 * Aggregates supplied objects into a List for matching against
	 * 
	 * <pre>
 	 * assertThat(Cases.of(Case.of((List&lt;Integer&gt; input) -&gt; input.size()==3, input -&gt; &quot;hello&quot;),
	 *			Case.of((List&lt;Integer&gt; input) -&gt; input.size()==2, input -&gt; &quot;ignored&quot;),
	 *			Case.of((List&lt;Integer&gt; input) -&gt; input.size()==1, input -&gt; &quot;world&quot;)).match(1,2,3).get(),is(&quot;hello&quot;));
     *
	 * </pre>
	 * 
	 * @param t Array to match on
	 * @return Matched value wrapped in Optional
	 */
	public  Optional<X> match(Object... t){
		return cse.getPatternMatcher().match(t);
	}
	/**
	 * @param t Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise Optional.empty()
	 */
	public  Optional<X> match(Object t){
		return cse.getPatternMatcher().match(t);
	}
	/**
	 * Immediately decompose the supplied parameter and pass it to the PatternMatcher for matching
	 * 
	 * @param decomposableObject Object to match on after decomposed via unapply method
	 * @return Matching result
	 */
	public Optional<X> unapply(Decomposable decomposableObject) {
		return cse.getPatternMatcher().unapply(decomposableObject);
	}
}
