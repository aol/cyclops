package com.aol.cyclops.matcher;

import java.util.function.Predicate;

public class Matchers {
	
	public final static<V> Predicate<V> identityMatcher(Object toMatch){
		return (t) -> {
			return toMatch == t;
				
		};
	}
	public final static<V> Predicate<V> instanceMatcher(Object toMatch){
		return (t) -> {
			return toMatch.equals(t);
				
		};
	}
	public final static<V> Predicate<V> rangeChecker(int start, int end){
		
		return (t) ->{
			
			if(!(t instanceof Integer))
				return false;
			Integer test = (Integer)t;
			return test>=start && test<end;
			
			
		};
	};
	public final static <V> Predicate<V> typeMatcher(Class c){
		return (t) ->{ 
			Class test = t.getClass();
			return c.isAssignableFrom(test);
		};
	};
}
