package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.PatternMatcher;

public abstract class Case {

	abstract Case withPatternMatcher(PatternMatcher matcher);
	abstract PatternMatcher getPatternMatcher();
}
