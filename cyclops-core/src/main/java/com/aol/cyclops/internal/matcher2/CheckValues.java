package com.aol.cyclops.internal.matcher2;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.hamcrest.Matcher;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.util.function.Predicates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;


@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class CheckValues<T,R> {
	private final Class<T> clazz;
	protected final MatchableCase<R> simplerCase;

	/**
	 * 
	 * Provide a comparison value, JDK 8 Predicate, or Hamcrest Matcher  for each Element to match on.
	 * 
	 * Further &amp; recursively unwrap any element by Predicates.type(ELEMENT_TYPE.class).with(V... values)
	 * 
	 * @see Predicates#type
	 * 
	 * @param values Matching rules for each element in the decomposed / unapplied user input
	 * @return Pattern Matcher builder with completed Case added to it
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V>CheckValues<T,R> values(Function<? super T,? extends R> result,V... values) {		
		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = ReactiveSeq.of(values)
				.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
				.toArray(new Predicate[0]);

		//return new _LastStep<R,V,T>(clazz,predicate,predicates,this.getPatternMatcher());
		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate,result,
				predicates)).withType(clazz);
	}
	@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V> CheckValues<T,R> just(Function<? super T,? extends R> result,V... values) {	
		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		@SuppressWarnings("unchecked")
		Predicate<V>[] predicates = ReactiveSeq.of(values)
				.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
				.toArray(new Predicate[0]);

		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
				predicates)).withType(clazz);
	}
	@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V> CheckValues<T,R> justWhere(Function<? super T,? extends R> result,Predicate<V>... values){
		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = ReactiveSeq.of(values)
				.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
				.toArray(new Predicate[0]);

	
		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
				predicates)).withType(clazz);
	}
	@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V> CheckValues<T,R> where(Function<? super T,? extends R> result,Predicate<V>... values) {
	
		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = ReactiveSeq.of(values)
				.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
				.toArray(new Predicate[0]);

		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
				predicates)).withType(clazz);
	}
	@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V> CheckValues<T,R> justMatch( Function<? super T,? extends R> result,Matcher<V>... values){
		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = ReactiveSeq.of(values)
				.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toListX().plus(i->SeqUtils.EMPTY==i)
				.toArray(new Predicate[0]);

		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
				predicates)).withType(clazz);
	}
	@SafeVarargs @SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V> CheckValues<T,R> match(Function<? super T,? extends R> result,Matcher<V>... values) {
		
		Predicate predicate = it -> Optional.of(it)
											.map(v -> v.getClass().isAssignableFrom(clazz))
											.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = ReactiveSeq.of(values)
											.map(nextValue -> simplerCase.convertToPredicate(nextValue)).toList()
											.toArray(new Predicate[0]);

		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
				predicates)).withType(clazz);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final <V> CheckValues<T,R> isEmpty(Function<? super T,? extends R> result) {

		Predicate predicate = it -> Optional.of(it)
				.map(v -> v.getClass().isAssignableFrom(clazz))
				.orElse(false);
		// add wildcard support
		
		Predicate<V>[] predicates = new Predicate[]{i->i==SeqUtils.EMPTY};

		return new MatchableCase(this.getPatternMatcher().inCaseOfManyType(predicate, result,
				predicates)).withType(clazz);
	}


	public PatternMatcher getPatternMatcher() {
		return simplerCase.getPatternMatcher();
	}
}

