package com.aol.cyclops.matcher.builders;

import java.util.Optional;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import org.jooq.lambda.Seq;

import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Predicates;


@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class _MembersMatchBuilder<X,T> {
	private final Class<T> clazz;
	private final _Simpler_Case<X> simplerCase;
	
	/**
	 * 
	 * Provide a comparison value, JDK 8 Predicate, or Hamcrest Matcher  for each Element to match on.
	 * 
	 * Further & recursively unwrap any element by Predicates.type(ELEMENT_TYPE.class).with(V... values)
	 * 
	 * @see Predicates#type
	 * 
	 * @param values Matching rules for each element in the decomposed / unapplied user input
	 * @return Pattern Matcher builder with completed Case added to it
	 */
	@SafeVarargs
	public final <V> _LastStep<X,V,T> with(V... values) {
		
		
		
	
		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = Seq.of(values)
				.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
				.toArray(new Predicate[0]);

		return new _LastStep<X,V,T>(clazz,predicate,predicates,this.getPatternMatcher());

	}


	public PatternMatcher getPatternMatcher() {
		return simplerCase.getPatternMatcher();
	}
}

