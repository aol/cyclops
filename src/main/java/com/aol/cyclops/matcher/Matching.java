package com.aol.cyclops.matcher;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.val;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.PatternMatcher.Action;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturnWrapper;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;

public class Matching {

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
	public static <V,T,X> TypeSafePatternMatcher<T,X> caseOf(Matcher<V> match,Action<V> a){
		return new TypeSafePatternMatcher<T,X>().caseOf(match,a);
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> caseOf(Predicate<V> match,Action<V> a){
		return new TypeSafePatternMatcher<T,X>().caseOf(match, a);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOfThenExtract(Predicate<V> match,Action<V> a, Extractor<T,R> extractor){
		
		return new TypeSafePatternMatcher<T,X>().caseOfThenExtract(match,a,extractor);
	}
	
	public  static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOfThenExtract(Matcher<V> match,Action<V> a, Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().caseOfThenExtract(match, a, extractor);
	}
	
	public  static <R,V,T,X> TypeSafePatternMatcher<T,X> caseOf( Extractor<T,R> extractor,Predicate<V> match,Action<V> a){
		
		return new TypeSafePatternMatcher<T,X>().caseOf(extractor, match, a);
	}
	public  static <R,V,T,X> TypeSafePatternMatcher<T,X>  caseOf( Extractor<T,R> extractor,Matcher<V> match,Action<V> a){
		return new TypeSafePatternMatcher<T,X>().caseOf(extractor, match, a);
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOfValue(V value,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfValue(value, a);
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOfType(ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfType(a);
		
	}
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOf(Predicate<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOf(match, a);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfThenExtract(Predicate<V> match,ActionWithReturn<V,X> a, Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().inCaseOfThenExtract(match, a, extractor);
	}
	
	
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOf( Extractor<T,R> extractor,Predicate<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOf(extractor, match, a);
	}
	
	public static <V,T,X> TypeSafePatternMatcher<T,X> inCaseOf(Matcher<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOf(match, a);
	}
	
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfThenExtract(Matcher<V> match,ActionWithReturn<V,X> a, Extractor<T,R> extractor){
		return new TypeSafePatternMatcher<T,X>().inCaseOfThenExtract(match, a, extractor);
	}
	
	
	public static  <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOf( Extractor<T,R> extractor,Matcher<V> match,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOf(extractor, match, a);
	}
	
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfType( Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfType(extractor, a);
	}
	public static <R,V,T,X> TypeSafePatternMatcher<T,X> inCaseOfValue(R value, Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		return new TypeSafePatternMatcher<T,X>().inCaseOfValue(value, extractor, a);
	}
	
}
