package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.PatternMatcher.Action;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturnWrapper;

public interface Step<T,X>{
	
	
	<X> MatchingInstance<T,X> thenApply(ActionWithReturn<T,X> t);
	default MatchingInstance<T,X> thenConsume(Action<T> t){
		return thenApply(new ActionWithReturnWrapper(t));
	}
}