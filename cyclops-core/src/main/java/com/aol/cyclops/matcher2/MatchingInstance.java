package com.aol.cyclops.matcher2;

import com.aol.cyclops.control.Maybe;

import lombok.AllArgsConstructor;
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
public class MatchingInstance <T, X> {
	
	
	private final CaseBeingBuilt cse;
	
	
	
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
	public  Maybe<X> match(Object... t){
		return cse.getPatternMatcher().match(t);
	}
	/**
	 * @param t Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise Optional.empty()
	 */
	public  Maybe<X> match(Object t){
		return cse.getPatternMatcher().match(t);
	}
	
	
}
