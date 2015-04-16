package com.aol.cyclops.matcher;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.matcher.PatternMatcher.Action;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;

public class Matching {
	//Iterable<Predicate<V>> predicates
	@SafeVarargs
	public static <R,V,T,X> InCaseOfManyStep2<R,V,T,X> caseOf(Predicate<V>... predicates) {
		return new InCaseOfManyStep2<R,V,T,X>(predicates);
		
	}
	
	@SafeVarargs
	public static <R,V,T,X> InMatchOfManyStep2<R,V,T,X> matchOf(Matcher<V>... predicates) {
		return new InMatchOfManyStep2<R,V,T,X>(predicates);
	}
	
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> selectFromChain(Stream<? extends ChainOfResponsibility<V,X>> stream){
		return new TypeSafePatternMatcher<T,X>().selectFromChain(stream);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T, X> selectFrom(Stream<Tuple2<Predicate<V>,Function<V,X>>> stream){
		return new TypeSafePatternMatcher<T,X>().selectFrom(stream);
	}

	public static <R,V,V1,T,X> TypeSafePatternMatcher<T, X> inMatchOf(
			Matcher<V> pred1, Matcher<V1> pred2,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {

		return new TypeSafePatternMatcher<T,X>().inMatchOfMatchers(Tuple.tuple(pred1,pred2), a, extractor);
	}

	public static <R,V,V1,T,X> TypeSafePatternMatcher<T, X> inCaseOf(
			Predicate<V> pred1, Predicate<V1> pred2,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {

		return new TypeSafePatternMatcher<T,X>().inCaseOfPredicates(Tuple.tuple(pred1,pred2), a, extractor);
	}

	public static <R,V,T,X> TypeSafePatternMatcher<T, X> inCaseOfTuple(Tuple predicates,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {

		return new TypeSafePatternMatcher<T,X>().inCaseOfTuple(predicates, a, extractor);
	}

	public static <R,V,T,X> TypeSafePatternMatcher<T, X> inMatchOfTuple(Tuple predicates,
			ActionWithReturn<R, X> a, Extractor<T, R> extractor) {
		return new TypeSafePatternMatcher<T,X>().inMatchOfTuple(predicates, a, extractor);
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

	
	/** core api **/
	public static <V,T,X> Step<ActionWithReturn<V,X>,TypeSafePatternMatcher<T,X>> isValue(V value){
		return (ActionWithReturn<V,X> a) -> new TypeSafePatternMatcher<T,X>().inCaseOfValue(value, a) ;
	}
	
	public static <V,T,X> Step<ActionWithReturn<List<V>,X>,TypeSafePatternMatcher<T,X>> isValues(V... values){
		Predicate<V>[] predicates = Seq.of(values).map(nextValue->Predicates.p(test->Objects.equals(test, nextValue))).toList().toArray(new Predicate[0]);
		return (ActionWithReturn<List<V>,X> a) -> new TypeSafePatternMatcher<T,X>().inCaseOfMany(a,predicates) ;
	}
	
	public static <V,T,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<T,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfType(a);
		
	}
	public static <V> InCaseOfBuilder<V> caseOf(Predicate<V> match){
		return new InCaseOfBuilder<V>(match);
	}
	
	public static <V> InMatchOfBuilder<V> matchOf(Matcher<V> match){
		
		return new InMatchOfBuilder<V>(match);
	}
	
	
	
	public static <T,R> InCaseOfBuilderExtractor<T,R> extract( Extractor<T,R> extractor){
		return new InCaseOfBuilderExtractor<T,R>(extractor);
	}
	
	@AllArgsConstructor
	public static class InCaseOfBuilder<V>{
		private final Predicate<V> match;
		
		
		public <T,R,X> Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<V,X>> thenExtract(Extractor<T,R> extractor){
			return (ActionWithReturn<R,X> a)->{
				return new TypeSafePatternMatcher<V,X>().inCaseOfThenExtract(match, a,(Extractor<V,R>)extractor);
			};
		}
		
		public <T,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>().inCaseOf(match, a);
		}
		public <T,X>  TypeSafePatternMatcher<T,X> thenConsume(Action<V> a){
			return new TypeSafePatternMatcher<T,X>().caseOf(match, a);
		}
		
	}
	@AllArgsConstructor
	public static class InCaseOfBuilderExtractor<T,R>{
		private final Extractor<T,R> extractor;
				//new TypeSafePatternMatcher<T,X>().inCaseOf(extractor, match, a);
		
		public <V,X> Step<ActionWithReturn<V,X>,TypeSafePatternMatcher<T,X>> caseOf(Predicate<V> match){
			return (ActionWithReturn<V,X> a)-> new TypeSafePatternMatcher<T,X>().inCaseOf(extractor, match, a);
		}
		public <V,X> Step<ActionWithReturn<V,X>,TypeSafePatternMatcher<T,X>> matchOf(Matcher<V> match){
			return (ActionWithReturn<V,X> a)-> new TypeSafePatternMatcher<T,X>().inMatchOf(extractor, match, a);
		}
		
		public <V,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>().inCaseOfType(extractor, a);
		}
		public <V,X> Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<T,X>>  isValue(R value){
			return (ActionWithReturn<R,X> a ) ->{
				return new TypeSafePatternMatcher<T,X>().inCaseOfValue(value, extractor, a);
			};
		}
		
	}
	
	@AllArgsConstructor
	public static class InMatchOfBuilder<V>{
		private final Matcher<V> match;
		
		public <R,X> Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<V,X>> thenExtract(Extractor<V,R> extractor){
			return (ActionWithReturn<R,X> a)->{
				return new TypeSafePatternMatcher<V,X>().inMatchOfThenExtract(match, a,extractor);
			};
		}
		public <T,X> TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<V,X> a){
			return new TypeSafePatternMatcher<T,X>().inMatchOf(match, a);
		}
		public <T,X>  TypeSafePatternMatcher<T,X> thenConsume(Action<V> a){
			return new TypeSafePatternMatcher<T,X>().matchOf(match, a);
		}
		
	}
	
	@AllArgsConstructor
	public static class InCaseOfManyStep2<R,V,T,X>{
		private final Predicate<V>[] predicates;
		
		public  TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<List<V>, X> a){
			return new TypeSafePatternMatcher<T,X>().inCaseOfMany( a,predicates);
		}
		public  TypeSafePatternMatcher<T,X> thenConsume(Action<List<V>> a){
			return new TypeSafePatternMatcher<T,X>().caseOfMany( a,predicates);
		}
	}

	public static interface Step<T,R>{
		R thenApply(T t);
		default void thenConsume(T t){
			thenApply(t);
		}
	}
	
	
	@AllArgsConstructor
	public static class InMatchOfManyStep2<R,V,T,X>{
		private final Matcher<V>[] predicates;
		
		public  TypeSafePatternMatcher<T,X> thenApply(ActionWithReturn<List<V>, X> a){
			return new TypeSafePatternMatcher<T,X>().inMatchOfMany( a,predicates);
		}
		public  TypeSafePatternMatcher<T,X> thenConsume(Action<List<V>> a){
			return new TypeSafePatternMatcher<T,X>().matchOfMany( a,predicates);
		}
		
	}
	
	
}
