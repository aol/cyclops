package com.aol.cyclops.internal.matcher2;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class CaseBeingBuilt {

	abstract CaseBeingBuilt withPatternMatcher(PatternMatcher matcher);
	abstract public PatternMatcher getPatternMatcher();
	public Predicate convertToPredicate(Object o){
		if(o instanceof Predicate)
			return (Predicate)o;
		
		if(o instanceof ADTPredicateBuilder)
			return ((ADTPredicateBuilder)o).toPredicate();
			
		return test -> Objects.equals(test,o);
	}
}
