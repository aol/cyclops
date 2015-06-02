package com.aol.cyclops.matcher.builders;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;
@AllArgsConstructor
public class CheckType<R> extends CaseBeingBuilt{
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;

	/**
	 * Create a completed Case which is triggered when matching input is of the same type (T) as the input parameter
	 *  to ActionWithReturn.
	 *  
	 *  The ActionWithReturn will then be executed and the result returned as the match result.
	 * 
	 * @param a Action for the new Case, Predicate for the Case will be created from the input type to the Action.
	 * @return Completed Case
	 */
	public  <T,R> CheckType<R> isType(ActionWithReturn<T,R> a){
		
		return (CheckType)this.withPatternMatcher(patternMatcher.inCaseOfType(a));
		
	}
	
}
