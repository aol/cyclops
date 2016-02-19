package com.aol.cyclops.matcher;


import java.util.function.Predicate;

public class TestPredicates {
	
	
	public final static Predicate instanceMatcher(Object toMatch){
		return (t) -> {
			return toMatch.equals(t);
				
		};
	}
	public final static Predicate rangeChecker(int start, int end){
		
		return (t) ->{ 
			if(!(t instanceof Integer))
				return false;
			Integer test = (Integer)t;
			return test>=start && test<end;
			
			
		};
	};
	public final static Predicate typeMatcher(Class c){
		return (t) ->{ 
			Class test = t.getClass();
			return c.isAssignableFrom(test);
		};
	};
}

