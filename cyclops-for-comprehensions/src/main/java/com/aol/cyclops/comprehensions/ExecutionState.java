package com.aol.cyclops.comprehensions;

import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ExecutionState<T1,T2,V> {
	
	public final ContextualExecutor<T1,T2> contextualExecutor;
	public final State state;
	
	
	
}
