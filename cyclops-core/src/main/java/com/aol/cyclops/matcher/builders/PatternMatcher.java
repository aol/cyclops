package com.aol.cyclops.matcher.builders;

import static com.aol.cyclops.matcher.builders.SeqUtils.seq;

import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.matcher2.Case;
import com.aol.cyclops.matcher2.Cases;
import com.aol.cyclops.matcher2.Extractor;
import com.aol.cyclops.matcher2.Extractors;
import com.aol.cyclops.objects.Decomposable;
import com.aol.cyclops.sequence.SequenceM;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;



/**
 * PatternMatcher supports advanced pattern matching for Java 8
 * 
 * This is an API for creating Case instances and allows new type definitions to be supplied for each Case
 * 
 * Features include
 * 
 * -cases match by value
 * -cases match by type
 * -cases using predicates
 *  		inCaseOfXXX
 *  		caseOfXXX
 * -cases using hamcrest Matchers
 * 			inMatchOfXXX
 * 			matchOfXXX
 * -cases as expressions (return value) - inCaseOfXXX, inMatchOfXXX
 * -cases as statements (no return value) - caseOfXXX, matchOfXXX
 * -pre &amp; post variable extraction via Extractor (@see com.aol.cyclops.matcher.Extractors)
 * -match using iterables of predicates or hamcrest Matchers
 * 		- see caseOfIterable, matchOfIterable, inCaseOfIterable, matchOfIterable
 * -match using tuples of predicates or hamcreate Matchers
 * 	 	- see caseOfTuple, matchOfTuple, inCaseOfTuple, inMatchOfTuple
 * 
 * - single match (match method)
 * - match many (matchMany)
 * - match against a stream (single match, match many)
 * 
 * @author johnmcclean
 *
 */
@SuppressWarnings("unchecked")
@AllArgsConstructor
public class PatternMatcher implements Function{
	
	@Wither @Getter
	private final Cases cases;
	
	public PatternMatcher(){
		cases = Cases.of();
	}
	/**
	 * @return Pattern Matcher as function that will return the 'unwrapped' result when apply is called.
	 *  i.e. Optional#get will be called.
	 * 
	 */
	public <T,X> Function<T,X> asUnwrappedFunction(){
		return cases.asUnwrappedFunction();
	}
	
	
	
	/* 
	 *	@param t Object to match against
	 *	@return Value from matched case if present
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public Maybe<Object> apply(Object t){
		return match(t);
	}
	
	
	/**
	 * Aggregates supplied objects into a List for matching against
	 * 
	 * 
	 * @param t Array to match on
	 * @return Matched value wrapped in Optional
	 */
	public <R> Maybe<R> match(Object... t){
		return cases.match(t);
	}
	/**
	 * Decomposes the supplied input via it's unapply method
	 * Provides a List to the Matcher of values to match on
	 * 
	 * @param t Object to decompose and match on
	 * @return Matched result wrapped in an Optional
	 */
	public <R> Maybe<R> unapply(Decomposable t){
		return cases.unapply(t);
	}
	/**
	 * @param t Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise Optional.empty()
	 */
	public <R> Maybe<R> match(Object t){
			
		return cases.match(t);
		
	}


	
	
	private Object extractIfType(Object t, Extractor extractor) {
		try {
			MethodType type = extractor.getType();
			if (type.parameterCount() == 0)
				return t; // can't get parameter types for MethodReferences
			return type.parameterType(type.parameterCount() - 1).isAssignableFrom(t.getClass()) ? extractor.apply(t)
					: t;

		} catch (ClassCastException e) { // MethodReferences will result in
											// ClassCastExceptions

		}
		return t;
	}

	private Predicate extractorPredicate(Extractor extractor, Predicate p) {
		if (extractor == null)
			return p;

		return t -> p.test(extractIfType(t, extractor));
	}

	private Function extractorAction(Extractor extractor, Function action) {
		if (extractor == null)
			return action;
		return input -> action.apply(extractor.apply(input));
	}

	public <T, V, X> PatternMatcher inCaseOfManyType(Predicate master, Function<? super T, ? extends X> a,
			Predicate<V>... predicates) {

		SequenceM<Predicate<V>> pred = SequenceM.of(predicates);

		return inCaseOf(it -> master.test(it)
				&& seq(Extractors.decompose().apply(it)).zip(pred, (a1, b1) -> Tuple.tuple(a1, b1))
						.map(t -> t.v2.test((V) t.v1)).allMatch(v -> v == true),
				a);

	}

	private List wrapInList(Object a) {
		if (a instanceof List)
			return (List) a;
		else
			return Arrays.asList(a);
	}
	
		
	public <V,X> PatternMatcher inCaseOf(Predicate<V> match,Function<? super V,? extends X> a){
		return inCaseOfThenExtract(match, a, null);
		
	}
	public <R,T,X> PatternMatcher inCaseOfThenExtract(Predicate<T> match,Function<? super R,? extends X> a, Extractor<T,R> extractor){
		
		return withCases(cases.append(index(),Case.of(match,extractorAction(extractor,a))));
		
	}
	
	private int index() {
		return cases.size();
	}


	
}
