package com.aol.cyclops.matcher.builders;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.TypedFunction;
import com.aol.cyclops.objects.Decomposable;

import lombok.AllArgsConstructor;


@Deprecated
public class CollectionMatchingInstance  <T, X> extends MatchingInstance<T,X> implements Function<T, Optional<X>> {
	
	
	private final CaseBeingBuilt cse;
	
	
	public CollectionMatchingInstance(CaseBeingBuilt cse ){
		super(cse);
		this.cse = cse;
	}	
	public final Cases<T,X,TypedFunction<T,X>> cases(){
		return this.cse.getPatternMatcher().getCases();
	}
	
	/**
	 * Create a builder for matching on the disaggregated elements of a collection.
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher per element
	 * 
	 * @return Iterable / Collection based Pattern Matching Builder
	 */
	public final IterableCase<X> whenIterable(){
		IterableCase cse = new IterableCase(this.cse.getPatternMatcher());
		return cse;
	}
	/**
	 * Create a builder that builds Pattern Matching Cases from Streams of data.
	 * 
	 * 
	 * @return Stream based Pattern Matching Builder
	 */
	public final StreamCase whenFromStream(){
		StreamCase cse = new StreamCase(this.cse.getPatternMatcher());
		return cse;
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
