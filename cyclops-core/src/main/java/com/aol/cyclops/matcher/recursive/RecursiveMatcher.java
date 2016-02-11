package com.aol.cyclops.matcher.recursive;

import java.lang.invoke.MethodType;
import java.util.function.Function;

import com.aol.cyclops.matcher.builders.CheckTypeAndValues;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher2.TypedFunction;

@Deprecated//use Matchable instead
public class RecursiveMatcher {
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final<USER_VALUE> CheckTypeAndValues<USER_VALUE> when(){
		CheckTypeAndValues cse = new  CheckTypeAndValues(new PatternMatcher());
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
	 * @see com.aol.cyclops.matcher2.Extractors#decompose
	 * @see com.aol.cyclops.matcher2.Extractors#registerDecompositionFunction
	 * 
	 * @param a Action from which the Predicate (by param type) and Function will be extracted to build a Pattern Matching case
	 * @return Next step in Case builder
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <X,T, R> CheckTypeAndValues<X>.AndMembersMatchBuilder<T, R> whenIsType(TypedFunction<T, R> a) {

		return  new  CheckTypeAndValues(new PatternMatcher()).isType(a);

	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <X,T, R> CheckTypeAndValues<X>.AndMembersMatchBuilder<T, R> whenIsType(Class<T> t,Function<T, R> f) {

		return  new  CheckTypeAndValues(new PatternMatcher()).isType(new TypedFunction<T,R>(){

			@Override
			public R apply(T t) {
				return f.apply(t);
			}
			
			public MethodType getType(){
				return MethodType.methodType(Void.class, t);
			}
		});

	}
}
