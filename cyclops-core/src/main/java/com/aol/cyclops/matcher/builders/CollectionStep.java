package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.builders.ActionWithReturnWrapper;
import com.aol.cyclops.matcher2.Action;
import com.aol.cyclops.matcher2.TypedFunction;

public interface CollectionStep<T,X>{
	
	
	/**
	 * Create a new Case with the supplied ActionWithReturn as the action
	 * 
	 * @param t Action to be executed when the new Case is triggered
	 * @return Pattern Matcher Builder
	 */
	<X> MatchingInstance<T,X> thenApply(TypedFunction<T,X> t);
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