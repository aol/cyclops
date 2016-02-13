package com.aol.cyclops.internal.matcher2;

import java.util.Objects;
import java.util.function.Predicate;

import org.hamcrest.Matcher;

public abstract class CaseBeingBuilt {

	abstract CaseBeingBuilt withPatternMatcher(PatternMatcher matcher);
	abstract PatternMatcher getPatternMatcher();
	Predicate convertToPredicate(Object o){
		if(o instanceof Predicate)
			return (Predicate)o;
		if(o instanceof Matcher)
			return test -> ((Matcher)o).matches(test);
		if(o instanceof ADTPredicateBuilder)
			return ((ADTPredicateBuilder)o).toPredicate();
			
		return test -> Objects.equals(test,o);
	}
}
