package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;

import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Extractor;
import com.aol.cyclops.matcher.Predicates;
import com.aol.cyclops.matcher.builders.CaseBuilder.InCaseOfManyStep2;
import com.aol.cyclops.matcher.builders.CaseBuilder.InMatchOfManyStep2;
@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class IterableCase<X> extends Case{
	// T : user input (type provided to match)
	// X : match response (thenApply)
	// R : extractor response
	// V : input for matcher / predicate
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	/** Match all elements against an Array or Iterable - user provided elements are disaggregated and matched by index**/
	@SafeVarargs
	public  final <V> InCaseOfManyStep2<V> allTrue(Predicate<V>... predicates) {
		return new InCaseOfManyStep2<V>(predicates,patternMatcher,this);
		
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
	public  final <R,V,V1,V2,T,X> ExtractionStep<T,R,X> threeTrue(Predicate<V> pred1, Predicate<V1> pred2,Predicate<V2> pred3){
		//return new TypeSafePatternMatcher<T,X>(patternMatcher).inCaseOfPredicates(Tuple.tuple(pred1,pred2), a, extractor);
		//extractor // then action
		return allTrueNoType(pred1,pred2,pred3);
		
		
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
						return  addCase(patternMatcher.inCaseOfSeq(Seq.of(predicates), t, extractor));
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
	



	@SafeVarargs
	public  final <R,V,T,X> InMatchOfManyStep2<R,V,T,X> allMatch(Matcher<V>... predicates) {
		return new InMatchOfManyStep2<R,V,T,X>(predicates,patternMatcher,this);
	}
	public  final <T,R,V,V1> ExtractionStep<T,R,X> bothMatch(Matcher<V> pred1, Matcher<V1> pred2){
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
						return addCase(patternMatcher.inMatchOfSeq(Seq.of(predicates), t, extractor));
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
	private <T,R> MatchingInstance<T,R> addCase(PatternMatcher o){
		return new MatchingInstance<>(this.withPatternMatcher(o));
	}

	private <V> Predicate<Object> buildPredicate(V nextValue) {
		if(Predicates.ANY()==nextValue)
			return Predicates.ANY();
		return Predicates.p(test->Objects.equals(test, nextValue));
	}
	
	
}
