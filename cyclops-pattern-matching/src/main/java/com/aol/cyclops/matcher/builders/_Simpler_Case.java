package com.aol.cyclops.matcher.builders;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.builders._Case.AndMembersMatchBuilder;
@AllArgsConstructor
public class _Simpler_Case<X> extends CaseBeingBuilt {
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	public  <T, R> _Case<X>.AndMembersMatchBuilder<T, R> isType(ActionWithReturn<T,R> a){
		return new _Case(patternMatcher).isType(a);
		
		
	}
}
