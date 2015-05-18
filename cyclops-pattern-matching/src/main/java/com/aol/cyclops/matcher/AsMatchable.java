package com.aol.cyclops.matcher;

import java.util.Objects;
import java.util.function.Function;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.SimplestCase;
import com.aol.cyclops.matcher.builders._Case;

public class AsMatchable {
	
	
	/**
	 * Coerce / wrap an Object as a Matchable instance
	 * This adds match / _match methods for pattern matching against the object
	 * 
	 * @param toCoerce Object to convert into a Matchable
	 * @return Matchable that adds functionality to the supplied object
	 */
	public static  Matchable asMatchable(Object toCoerce){
		return new CoercedMatchable(toCoerce);
	}
	@AllArgsConstructor
	public static class CoercedMatchable implements Matchable{
		private final Object matchable;
	}
}
