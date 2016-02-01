package com.aol.cyclops.matcher.builders;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.Extractor;
import com.aol.cyclops.matcher.Predicates;
import com.aol.cyclops.matcher.Two;
import com.aol.cyclops.matcher.TypedFunction;
import com.aol.cyclops.sequence.SequenceM;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

/**
 * Case builder for handling iterables
 * Predicates match against each element in the Iterable
 * Extractor can be used to select iterable elements
 * Use Extractors.same to pass the iterable itself
 * 
 * 
 * @author johnmcclean
 *
 * @param <X> Type to be passed to match
 */
@AllArgsConstructor
@Deprecated
public class IterableCase<X> extends CaseBeingBuilt{
	// T : user input (type provided to match)
	// X : match response (thenApply)
	// R : extractor response
	// V : input for matcher / predicate
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	/** Match all elements against an Array or Iterable - user provided elements are disaggregated and matched by index**/
	/**
	 * All of the predicates hold
	 * Each predicate will be matched in turn against a member of the iterable
	 * Note if there is more elements in the iterable than predicates, and all predicates hold this case will trigger
	 * 
	 * 
	 * @param predicates Predicates to test the elements of provided iterable against
	 * @return Next stage in Case Step Builder
	 */
	@SafeVarargs
	public  final <V> InCaseOfManyStep2<V> allTrue(Predicate<V>... predicates) {
		return new InCaseOfManyStep2<V>(predicates,patternMatcher,this);
		
	}
	
	/**
	 * Check that two predicates accepting potential different types hold
	 * Will check against the first two elements of an iterable only.
	 * 
	 * @param pred1 Predicate to match against the first element in the iterable 
	 * @param pred2 Predicate to match against the second element in the iterable
	 * @return Next stage in the Case Step Builder
	 */
	public  final <R,V,V1,T,X> ExtractionStep<T,R,X> bothTrue(Predicate<V> pred1, Predicate<V1> pred2){

		return  new  ExtractionStep<T,R,X>(){

			/* 
			 * To keep the iterable as the value for the next step use
			 * @see Extractors#same	
			 * @see com.aol.cyclops.matcher.builders.ExtractionStep#thenExtract(com.aol.cyclops.matcher.Extractor)
			 */
			@Override
			public <T, R> TempCollectionStepExtension<R, X> thenExtract(Extractor<T, R> extractor) {
				
				return new TempCollectionStepExtension<R,X>(){
					
					@Override
					public <X> CollectionMatchingInstance<R, X> thenApply(TypedFunction<R, X> t) {
						
						return addCase(patternMatcher.inCaseOfPredicates(Two.tuple(pred1,pred2), t, extractor));
					}
				};
			}
			 
		};
		
		
	}
	/**
	 * 
	 *  Check that three predicates accepting potential different types hold
	 * Will check against the first three elements of an iterable only.
	 * 
	 * @param pred1 Predicate to match against the first element in the iterable 
	 * @param pred2 Predicate to match against the second element in the iterable 
	 * @param pred3 Predicate to match against the third element in the iterable 
	 * @return Next stage in Case Step Builder
	 */
	public  final <R,V,V1,V2,T,X> ExtractionStep<T,R,X> threeTrue(Predicate<V> pred1, Predicate<V1> pred2,Predicate<V2> pred3){

		return allTrueNoType(pred1,pred2,pred3);
		
		
	}
	/**
	 *  Each predicate will be matched in turn against a member of the iterable
	 * Note if there is more elements in the iterable than predicates, and all predicates hold this case will trigger
	 * 
	 * 
	 * @param predicates Predicates to test the elements of provided iterable against
	 * @return Next stage in Case Step Builder
	 * 
	 */
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allTrueNoType(Predicate<? extends Object>...predicates){
		//extractor // then action
		return  new  ExtractionStep<T,R,X>(){

			/* 
			 * To keep the iterable as the value for the next step use
			 * @see Extractors#same	
			 * @see com.aol.cyclops.matcher.builders.ExtractionStep#thenExtract(com.aol.cyclops.matcher.Extractor)
			 */
			@Override
			public <T, R> TempCollectionStepExtension<R, X> thenExtract(Extractor<T, R> extractor) {
				
				return new TempCollectionStepExtension<R,X>(){
					
					/* 
					 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.ActionWithReturn)
					 */
					@Override
					public <X> CollectionMatchingInstance<R, X> thenApply(TypedFunction<R, X> t) {
						return  addCase(patternMatcher.inCaseOfStream(Stream.of(predicates), t, extractor));
					}
				};
			}
			 
		};
			
	}
	
	/**
	 * Each supplied value will be checked against an element from the iterable
	 * Each supplied value could be a comparison value, JDK 8 Predicate, or Hamcrest Matcher
	 *  Note if there is more elements in the iterable than predicates (or matchers / prototype values etc), and all predicates hold this case will trigger
	 * 
	 * @param predicates comparison value, JDK 8 Predicate, or Hamcrest Matcher to compare to elements in an Iterable
	 * @return Next stage in the Case Step builder
	 */
	@SafeVarargs
	public  final <R,V,T> ExtractionStep<T,R,X> allHoldNoType(Object...predicates){
		return allTrueNoType(convert(predicates));
	}
	
	private Predicate[] convert(Object[] predicates) {
		return Stream.of(predicates).map(this::convertToPredicate).collect(Collectors.toList()).toArray(new Predicate[0]);
		
	}
	



	/**
	 * Each supplied Hamcrest Matcher will be matched against elements in the matching iterable
	 *  Note if there is more elements in the iterable than matchers, and all predicates hold this case will trigger
	 * 
	 * @param predicates Hamcrest Matchers to be matched against elements in the matching iterable
	 * @return Next stage in the Case Step builder
	 */
	@SafeVarargs
	public  final <R,V,T,X> InMatchOfManyStep2<R,V,T,X> allMatch(Matcher<V>... predicates) {
		return new InMatchOfManyStep2<R,V,T,X>(predicates,patternMatcher,this);
	}
	/**
	 * Check that two Matchers accepting potential different types hold
	 * Will check against the first two elements of an iterable only.
	 * 
	 * @param pred1 Matcher to match against the first element in the iterable 
	 * @param pred2 Matcher to match against the second element in the iterable
	 * @return Next stage in the Case Step Builder
	 */
	public  final <T,R,V,V1> ExtractionStep<T,R,X> bothMatch(Matcher<V> pred1, Matcher<V1> pred2){
		
		return  new  ExtractionStep<T,R,X>(){

			/* 
			 * To keep the iterable as the value for the next step use
			 * @see Extractors#same	
			 * @see com.aol.cyclops.matcher.builders.ExtractionStep#thenExtract(com.aol.cyclops.matcher.Extractor)
			 */
			@Override
			public <T, R> TempCollectionStepExtension<R, X> thenExtract(Extractor<T, R> extractor) {
				
				return new TempCollectionStepExtension<R,X>(){
					
					/* 
					 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.ActionWithReturn)
					 */
					@Override
					public <X> CollectionMatchingInstance<R, X> thenApply(TypedFunction<R, X> t) {
						
						return addCase(patternMatcher.inMatchOfMatchers(Two.tuple(pred1,pred2), t, extractor));
					}
				};
			}
			 
		};
	}
	/**
	 * Each supplied Hamcrest Matcher will be matched against elements in the matching iterable
	 *  Note if there is more elements in the iterable than matchers, and all predicates hold this case will trigger
	 * 
	 * @param predicates Hamcrest Matchers to be matched against elements in the matching iterable
	 * @return Next stage in the Case Step builder
	 */
	@SafeVarargs
	public  final <R,V,T,X> ExtractionStep<T,R,X> allMatchNoType(Matcher...predicates){
		
		return  new  ExtractionStep<T,R,X>(){

			/* 
			 * To keep the iterable as the value for the next step use
			 * @see Extractors#same	
			 * @see com.aol.cyclops.matcher.builders.ExtractionStep#thenExtract(com.aol.cyclops.matcher.Extractor)
			 */
			@Override
			public <T, R> TempCollectionStepExtension<R, X> thenExtract(Extractor<T, R> extractor) {
				return  new TempCollectionStepExtension<R,X>(){

					/* 
					 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.ActionWithReturn)
					 */
					@Override
					public <X> CollectionMatchingInstance<R, X> thenApply(TypedFunction<R, X> t) {
						return addCase(patternMatcher.inMatchOfSeq(Stream.of(predicates), t, extractor));
					}
					
				};
			}
			
		};
		
		
		
	}
	
	/**
	 * Check all supplied values against elements in the iterable in turn
	 *  Note if there is more elements in the iterable than values to match against and all values match this case will trigger
	 * 
	 * @param values to match against (via Objects.equals)
	 * @return Next stage in the Case Step builder
	 */
	@SafeVarargs
	public  final <V,T,X> TempCollectionStepExtension<List<V>,X> allValues(V... values){
		//add wildcard support
		Predicate<V>[] predicates = SequenceM.of(values).map(nextValue->buildPredicate(nextValue)).toList().toArray(new Predicate[0]);
		return new  TempCollectionStepExtension<List<V>,X>(){

			/* 
			 * @see com.aol.cyclops.matcher.builders.Step#thenApply(com.aol.cyclops.matcher.ActionWithReturn)
			 */
			@Override
			public <X> CollectionMatchingInstance<List<V>, X> thenApply(
					TypedFunction<List<V>, X> t) {
				return  addCase(patternMatcher.inCaseOfMany(t,predicates)) ;
			}
			
		};
	}
	private <T,R> CollectionMatchingInstance<T,R> addCase(PatternMatcher o){
		return new CollectionMatchingInstance<>(this.withPatternMatcher(o));
	}

	private <V> Predicate<Object> buildPredicate(V nextValue) {
		if(Predicates.ANY()==nextValue)
			return Predicates.ANY();
		return Predicates.p(test->Objects.equals(test, nextValue));
	}
	
	
}
