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
public class AtomisedCase<T> extends Case{
	// T : user input (type provided to match)
	// X : match response (thenApply)
	// R : extractor response
	// V : input for matcher / predicate
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
		return  new  ExtractionStep<T,R,X>(){

			@Override
			public <T, R> Step<R, X> thenExtract(Extractor<T, R> extractor) {
				
				return new Step<R,X>(){
					
					@Override
					public <X> MatchingInstance<R, X> thenApply(ActionWithReturn<R, X> t) {
						
						return addCase(patternMatcher.inCaseOfPredicates(Tuple.tuple(pred1,pred2), t, extractor));
					}
				};
			}
			 
		};
		
		
	}
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allTrueNoType(Predicate<? extends Object>...predicates){
		//extractor // then action
		return  new  ExtractionStep<T,R,X>(){

			@Override
			public <T, R> Step<R, X> thenExtract(Extractor<T, R> extractor) {
				
				return new Step<R,X>(){
					
					@Override
					public <X> MatchingInstance<R, X> thenApply(ActionWithReturn<R, X> t) {
						return  addCase(patternMatcher.inCaseOfTuple(Tuple.tuple(predicates), t, extractor));
					}
				};
			}
			 
		};
			
	}
	
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allHoldNoType(Object...predicates){
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
	public  final <R,V,T,X> InMatchOfManyStep2<R,V,T,X> allMatch(Matcher<V>... predicates) {
		return new InMatchOfManyStep2<R,V,T,X>(predicates,patternMatcher);
	}
	public  final <R,V,V1,X> ExtractionStep<T,R,X> bothMatch(Matcher<V> pred1, Matcher<V1> pred2){
		//return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfPredicates(Tuple.tuple(pred1,pred2), a, extractor);
		//extractor // then action
		return  new  ExtractionStep<T,R,X>(){

			@Override
			public <T, R> Step<R, X> thenExtract(Extractor<T, R> extractor) {
				
				return new Step<R,X>(){
					
					@Override
					public <X> MatchingInstance<R, X> thenApply(ActionWithReturn<R, X> t) {
						// TODO Auto-generated method stub
						return addCase(patternMatcher.inMatchOfMatchers(Tuple.tuple(pred1,pred2), t, extractor));
					}
				};
			}
			 
		};
	}
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allMatchNoType(Matcher...predicates){
		//extractor // then action
		return  new  ExtractionStep<T,R,X>(){

			@Override
			public <T, R> Step<R, X> thenExtract(Extractor<T, R> extractor) {
				return  new Step<R,X>(){

					@Override
					public <X> MatchingInstance<R, X> thenApply(ActionWithReturn<R, X> t) {
						return addCase(patternMatcher.inMatchOfTuple(Tuple.tuple(predicates), t, extractor));
					}
					
				};
			}
			
		};
		
		
		
	}
	
	@SafeVarargs
	public  final <V,T,X> Step<List<V>,X> allValues(V... values){
		//add wildcard support
		Predicate<V>[] predicates = Seq.of(values).map(nextValue->buildPredicate(nextValue)).toList().toArray(new Predicate[0]);
		return new  Step<List<V>,X>(){

			@Override
			public <X> MatchingInstance<List<V>, X> thenApply(
					ActionWithReturn<List<V>, X> t) {
				return  addCase(patternMatcher.inCaseOfMany(t,predicates)) ;
			}
			
		};
	}
	private <T,R> MatchingInstance<T,R> addCase(Object o){
		return new MatchingInstance<>(this);
	}

	public static final Predicate ANY = test ->true;

	private <V> Predicate<Object> buildPredicate(V nextValue) {
		if(ANY==nextValue)
			return ANY;
		return Predicates.p(test->Objects.equals(test, nextValue));
	}
	
}
