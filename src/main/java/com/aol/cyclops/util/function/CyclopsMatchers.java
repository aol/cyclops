package com.aol.cyclops.util.function;
import java.util.function.Predicate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class CyclopsMatchers {
	/**
	 * wildcard predicate
	 * 
	 */
	public static final Matcher __ = Matchers.any(Object.class);
	
	public	static<V> Matcher<V> hasValues(V... values){
		Predicate p = Predicates.has(values);
		return new Matcher(){

			@Override
			public void describeTo(Description description) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean matches(Object item) {
				return p.test(item);
			}

			@Override
			public void describeMismatch(Object item, Description mismatchDescription) {
				
				
			}

			@Override
			public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
				
				
			}
			
		};
		
	}
	public	static<V> Matcher hasValuesWhere(Predicate<V>... values){
		Predicate p = Predicates.hasWhere(values);
		return new Matcher(){

			@Override
			public void describeTo(Description description) {
				
				
			}

			@Override
			public boolean matches(Object item) {
				return p.test(item);
			}

			@Override
			public void describeMismatch(Object item, Description mismatchDescription) {
				
				
			}

			@Override
			public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
				
				
			}
			
		};
	}
	public	static<V> Matcher hasValuesMatch(Matcher<V>... values){
		Predicate p = Predicates.hasMatch(values);
		return new Matcher(){

			@Override
			public void describeTo(Description description) {
				
				
			}

			@Override
			public boolean matches(Object item) {
				return p.test(item);
			}

			@Override
			public void describeMismatch(Object item, Description mismatchDescription) {
				
				
			}

			@Override
			public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
				
				
			}
			
		};
	}
}
