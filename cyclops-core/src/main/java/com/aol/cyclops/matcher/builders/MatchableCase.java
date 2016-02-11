package com.aol.cyclops.matcher.builders;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
@AllArgsConstructor
public class MatchableCase<X> extends CaseBeingBuilt {
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	public  <T> CheckValues<X,T> withType(Class<T> type){
		return new CheckValues(type,this);
		
		
	}
	
	
	
}
