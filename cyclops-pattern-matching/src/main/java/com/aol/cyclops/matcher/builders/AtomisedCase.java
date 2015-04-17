package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;

import com.aol.cyclops.matcher.PatternMatcher;
import com.aol.cyclops.matcher.Predicates;
import com.aol.cyclops.matcher.TypeSafePatternMatcher;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfManyStep2;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfManyStep2;
@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class AtomisedCase extends Case{
	
	@Getter(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	/** Match all elements against an Array or Iterable - user provided elements are disaggregated and matched by index**/
	@SafeVarargs
	public  final <R,V,T,X> InCaseOfManyStep2<R,V,T,X> allTrue(Predicate<V>... predicates) {
		return new InCaseOfManyStep2<R,V,T,X>(predicates,patternMatcher);
		
	}
	
	public  final <R,V,V1,T,X> ExtractionStep<T,R,X> bothTrue(Predicate<V> pred1, Predicate<V1> pred2){
		//return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfPredicates(Tuple.tuple(pred1,pred2), a, extractor);
		//extractor // then action
		return  (Extractor<T, R> extractor)-> (ActionWithReturn<R, X> a) ->new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfPredicates(Tuple.tuple(pred1,pred2), a, extractor);
	}
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allTrueNoType(Predicate...predicates){
		//extractor // then action
		return (Extractor<T, R> extractor) -> (ActionWithReturn<R, X> a) -> new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfTuple(Tuple.tuple(predicates), a, extractor);
	}
	
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allNoType(Object...predicates){
		return allTrueNoType(convert(predicates));
	}
	
	private Predicate[] convert(Object[] predicates) {
		return Stream.of(predicates).map(this::convertToPredicate).collect(Collectors.toList()).toArray(new Predicate[0]);
		
	}
	private Predicate convertToPredicate(Object o){
		if(o instanceof Predicate)
			return (Predicate)o;
		if(o instanceof Matcher)
			return test -> ((Matcher)o).matches(test);
			
		return test -> Objects.equals(test,o);
	}



	@SafeVarargs
	public  final <R,V,T,X> InMatchOfManyStep2<R,V,T,X> allMatches(Matcher<V>... predicates) {
		return new InMatchOfManyStep2<R,V,T,X>(predicates,patternMatcher);
	}
	public  final <R,V,V1,T,X> ExtractionStep<T,R,X> bothMatch(Matcher<V> pred1, Matcher<V1> pred2){
		//return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfPredicates(Tuple.tuple(pred1,pred2), a, extractor);
		//extractor // then action
		return  (Extractor<T, R> extractor)-> (ActionWithReturn<R, X> a) ->new TypeSafePatternMatcher<T,X>(patternMatcher).inMatchOfMatchers(Tuple.tuple(pred1,pred2), a, extractor);
	}
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allMatchNoType(Matcher...predicates){
		//extractor // then action
		return (Extractor<T, R> extractor) -> (ActionWithReturn<R, X> a) -> new TypeSafePatternMatcher<T,X>(patternMatcher).inMatchOfTuple(Tuple.tuple(predicates), a, extractor);
	}
	
	@SafeVarargs
	public  final <V,T,X> Step<ActionWithReturn<List<V>,X>,TypeSafePatternMatcher<T,X>> allValues(V... values){
		//add wildcard support
		Predicate<V>[] predicates = Seq.of(values).map(nextValue->buildPredicate(nextValue)).toList().toArray(new Predicate[0]);
		return (ActionWithReturn<List<V>,X> a) -> new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfMany(a,predicates) ;
	}


	public static final Predicate ANY = test ->true;

	private <V> Predicate<Object> buildPredicate(V nextValue) {
		if(ANY==nextValue)
			return ANY;
		return Predicates.p(test->Objects.equals(test, nextValue));
	}
	
}
