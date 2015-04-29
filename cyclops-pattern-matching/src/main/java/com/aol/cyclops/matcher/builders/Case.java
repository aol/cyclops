package com.aol.cyclops.matcher.builders;

import java.util.Objects;
import java.util.function.Predicate;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.PatternMatcher;

public abstract class Case {

	abstract Case withPatternMatcher(PatternMatcher matcher);
	abstract PatternMatcher getPatternMatcher();
	Predicate convertToPredicate(Object o){
		if(o instanceof Predicate)
			return (Predicate)o;
		if(o instanceof Matcher)
			return test -> ((Matcher)o).matches(test);
		if(o instanceof With)
			return ((With)o).toPredicate();
			
		return test -> Objects.equals(test,o);
	}
}
