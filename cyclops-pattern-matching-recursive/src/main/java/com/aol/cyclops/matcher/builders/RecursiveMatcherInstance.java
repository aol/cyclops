package com.aol.cyclops.matcher.builders;

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.aol.cyclops.matcher.TypedFunction;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.builders.CaseBeingBuilt;
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
public class RecursiveMatcherInstance <T, X> implements Function<T, Optional<X>> {
	
	
	private final CaseBeingBuilt cse;
	
	
	
	public final Cases<T,X,TypedFunction<T,X>> cases(){
		return this.cse.getPatternMatcher().getCases();
	}
	/**
	 * Create a builder for Matching on Case classes. This is the closest builder
	 * for Scala / ML style pattern matching.
	 * 
	 * Case classes can be constructed succintly in Java with Lombok or jADT
	 * e.g.
	 * <pre>{@code
	 * \@Value final class CaseClass implements Decomposable { int field1; String field2;}
	 * }</pre>
	 * 
	 * Use with static imports from the Predicates class to get wildcards via '__' or ANY()
	 * And to apply nested / recursive matching via Predicates.type(  ).with (   )
	 * 
	 * Match disaggregated elements by type, value, JDK 8 Predicate or Hamcrest Matcher
	 * 
	 * @return Case Class style Pattern Matching Builder
	 */
	public final CheckTypeAndValues<X> when(){
		CheckTypeAndValues cse = new CheckTypeAndValues(this.cse.getPatternMatcher());
		return cse;
	}

	/**
	 * Build a Case where we will check if user input matches the Type of the input params on the ActionWithReturn instance supplied
	 * If it does, the ActionWithReturn will be executed (applied) to get the result of the Match.
	 * 
	 * isType will attempt to match on the type of the supplied Case class. If it matches the Case class will be 'decomposed' via it's unapply method
	 * and the Case will then attempt to match on each of the elements that make up the Case class. If the Case class implements Decomposable, that interface and it's
	 * unapply method will be used. Otherwise in Extractors it is possible to register Decomposition Funcitons that will unapply Case classes from other sources (e.g.
	 * javaslang, jADT or even Scala). If no Decomposition Function has been registered, reflection will be used to call an unapply method on the Case class if it exists.
	 * 
	 * @see com.aol.cyclops.matcher.Extractors#decompose
	 * @see com.aol.cyclops.matcher.Extractors#registerDecompositionFunction
	 * 
	 * @param a Action from which the Predicate (by param type) and Function will be extracted to build a Pattern Matching case
	 * @return Next step in Case builder
	 */
	public final <T,R> CheckTypeAndValues<X>.AndMembersMatchBuilder<T, R> whenIsType(TypedFunction<T, R> a) {

		return  new  CheckTypeAndValues(this.cse.getPatternMatcher()).isType(a);

	}
	public  final <T,R> CheckTypeAndValues<X>.AndMembersMatchBuilder<T, R> whenIsType(Class<T> t,Function<T, R> f) {

		return  new  CheckTypeAndValues(this.cse.getPatternMatcher()).isType(new TypedFunction<T,R>(){

			@Override
			public R apply(T t) {
				return f.apply(t);
			}
			
			public MethodType getType(){
				return MethodType.methodType(Void.class, t);
			}
		});

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
