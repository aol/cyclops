package com.aol.cyclops.react.async;

import com.aol.cyclops.react.stream.traits.Continuation;

public interface ContinuationStrategy {

	public void addContinuation(Continuation c);
	public void handleContinuation();
}
