package com.aol.cyclops.matcher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.matcher.PatternMatcher.Action;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;

public class Matching {
	//Iterable<Predicate<V>> predicates
	@SafeVarargs
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> inCaseOfMany(
			ActionWithReturn<List<V>, X> a,Predicate<V>... predicates) {

		return new TypeSafePatternMatcher<T,X>().inCaseOfMany( a,predicates);
	}
	@SafeVarargs
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> inMatchOfMany(
			 ActionWithReturn<List<V>, X> a,Matcher<V>... predicates) {
		return new TypeSafePatternMatcher<T,X>().inMatchOfMany(a,predicates);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> selectFromChain(Stream<ChainOfResponsibility<V,X>> stream){
		return new TypeSafePatternMatcher<T,X>().selectFromChain(stream);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> selectFrom(Stream<Tuple2<Predicate<V>,Function<V,X>>> stream){
		return new TypeSafePatternMatcher<T,X>().selectFrom(stream);
	}

	public static <R,V,V1,T,X> TypeSafePatternMatcher<T, X> inMatchOfTuple(
			Tuple2<Matcher<V>, Matcher<V1>> predicates,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {

		return new TypeSafePatternMatcher<T,X>().inMatchOfMatchers(predicates, a, extractor);
	}

	public static <R,V,V1,T,X> TypeSafePatternMatcher<T, X> inCaseOfPredicates(
			Tuple2<Predicate<V>, Predicate<V1>> predicates,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {

		return new TypeSafePatternMatcher<T,X>().inCaseOfPredicates(predicates, a, extractor);
	}

	public static <R,V,T,X> TypeSafePatternMatcher<T, X> inCaseOfTuple(Tuple predicates,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {

		return new TypeSafePatternMatcher<T,X>().inCaseOfTuple(predicates, a, extractor);
	}

	public static <R,V,T,X> TypeSafePatternMatcher<T, X> inMatchOfTuple(Tuple predicates,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {
		return new TypeSafePatternMatcher<T,X>().inMatchOfTuple(predicates, a, extractor);
	}
	@SafeVarargs
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> caseOfMany(Action<List<V>> a,Predicate<V>... predicates){
		return new TypeSafePatternMatcher<T,X>().caseOfMany(a,predicates);
		
	}
	@SafeVarargs
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> matchOfMany(Action<List<V>> a,Matcher<V>... predicates){
		return new TypeSafePatternMatcher<T,X>().matchOfIterable(a,predicates);
		
	}
	
	public static <R,V,V1,T,X>  TypeSafePatternMatcher<T, X> matchOfMatchers(Tuple2<Matcher<V>,Matcher<V1>> predicates,
			Action<R> a,Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().matchOfMatchers(predicates, a, extractor);
		
	}
	
	public static <R,V,V1,T,X> TypeSafePatternMatcher<T, X> caseOfPredicates(Tuple2<Predicate<V>,Predicate<V1>> predicates,
			Action<R> a,Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().caseOfPredicates(predicates, a, extractor);
		
	}
			
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> caseOfTuple(Tuple predicates, Action<R> a,Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().caseOfTuple(predicates, a, extractor);
		
	}
			
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> matchOfTuple(Tuple predicates, Action<R> a,Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().matchOfTuple(predicates, a, extractor);
	}

	public static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOfType( Extractor<T,R> extractor,Action<V> a){
		return new TypeSafePatternMatcher<T,X>().caseOfType(extractor,a);
		
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOfValue(R value, Extractor<T,R> extractor,Action<V> a){
		
		return new TypeSafePatternMatcher<T,X>().caseOfValue(value,extractor,a);
	}
	
	
	public static <V,T,X> TypeSafePatternMatcher<T,X> caseOfValue(V value,Action<V> a){
		
		return new TypeSafePatternMatcher<T,X>().caseOfValue(value,a);
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> caseOfType(Action<V> a){
		return new TypeSafePatternMatcher<T,X>().<V>caseOfType(a);
		
	}

	public static <V,T,X> TypeSafePatternMatcher<T,X> caseOf(Predicate<V> match,Action<V> a){
		return new TypeSafePatternMatcher<T,X>().caseOf(match, a);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOfThenExtract(Predicate<V> match,Action<R> a, Extractor<T,R> extractor){
		
		return new TypeSafePatternMatcher<T,X>().caseOfThenExtract(match,a,extractor);
	}
	
	
	public  static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOf( Extractor<T,R> extractor,Predicate<R> match,Action<V> a){
		
		return new TypeSafePatternMatcher<T,X>().caseOf(extractor, match, a);
	}
	
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOfValue(V value,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfValue(value, a);
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOfType(ActionWithReturn<T,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfType(a);
		
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOf(Predicate<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOf(match, a);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfThenExtract(Predicate<T> match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().inCaseOfThenExtract(match, a, extractor);
	}
	
	
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOf( Extractor<T,R> extractor,Predicate<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOf(extractor, match, a);
	}
	

	
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfType( Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfType(extractor, a);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfValue(R value, Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfValue(value, extractor, a);
	}
	
	
	public  static <R,T,X> TypeSafePatternMatcher<T,X>  matchOf( Extractor<T,R> extractor,Matcher<R> match,Action<R> a){
		return new TypeSafePatternMatcher<T,X>().matchOf(extractor, match, a);
	}
	
	public static <V,T,X> TypeSafePatternMatcher<T,X> matchOf(Matcher<V> match,Action<V> a){
		return new TypeSafePatternMatcher<T,X>().matchOf(match,a);
	}
	
	public  static <R,V,T,X> TypeSafePatternMatcher<T,X> matchOfThenExtract(Matcher<V> match,Action<R> a, Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().matchOfThenExtract((Matcher)match, a, extractor);
	}
	
	
	
	public static <V,T,X> TypeSafePatternMatcher<T,X> inMatchOf(Matcher<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inMatchOf(match, a);
	}
	
	public static <R,T,X> TypeSafePatternMatcher<T,X> inMatchOfThenExtract(Matcher<T> match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().inMatchOfThenExtract(match, a, extractor);
	}
	
	
	public static  <R,V,T,X> TypeSafePatternMatcher<T,X> inMatchOf( Extractor<T,R> extractor,Matcher<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inMatchOf(extractor, match, a);
	}
	
}
