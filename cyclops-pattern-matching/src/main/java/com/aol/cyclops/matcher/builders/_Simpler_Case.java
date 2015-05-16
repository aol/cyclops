package com.aol.cyclops.matcher.builders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import org.jooq.lambda.Seq;

import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Predicates;
@AllArgsConstructor
public class _Simpler_Case<X> extends CaseBeingBuilt {
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	public  <T> AndMembersMatchBuilder<T> withType(Class<T> type){
		return new AndMembersMatchBuilder(type);
		
		
	}
	@AllArgsConstructor
	public class AndMembersMatchBuilder<T> {
		Class<T> clazz;

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
		public final <V> LastStep<X,V,T> with(V... values) {
			
			
			
		
			Predicate predicate = it -> Optional.of(it)
					.map(v -> v.getClass().isAssignableFrom(clazz))
					.orElse(false);
			// add wildcard support
			
			Predicate<V>[] predicates = Seq.of(values)
					.map(nextValue -> convertToPredicate(nextValue)).toList()
					.toArray(new Predicate[0]);

			return new LastStep<X,V,T>(predicate,predicates);

		}
	}
	
	@AllArgsConstructor
	public class LastStep<X,V,T> {
		Predicate predicate;
		Predicate<V>[] predicates;
		public final <X> _Simpler_Case<X> then(ActionWithReturn<T,X> fn){
			return new _Simpler_Case(patternMatcher.inCaseOfManyType(predicate, fn,
					predicates));
		}
	}
	
	
}
