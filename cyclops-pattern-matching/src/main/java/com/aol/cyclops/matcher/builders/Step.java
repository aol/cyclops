package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.Action;
import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.builders.PatternMatcher.ActionWithReturnWrapper;

public interface Step<T,X>{
	
	
	/**
	 * Create a new Case with the supplied ActionWithReturn as the action
	 * 
	 * @param t Action to be executed when the new Case is triggered
	 * @return Pattern Matcher Builder
	 */
	<X> MatchingInstance<T,X> thenApply(ActionWithReturn<T,X> t);
	/**
	 * Create a new Case with the supplied ActionWithReturn as the action
	 * 
	 * @param t Action to be executed when the new Case is triggered
	 * @return Pattern Matcher Builder
	 */
	default MatchingInstance<T,X> thenConsume(Action<T> t){
		return thenApply(new ActionWithReturnWrapper(t));
	}
}