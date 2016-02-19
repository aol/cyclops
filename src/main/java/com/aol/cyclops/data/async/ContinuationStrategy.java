package com.aol.cyclops.data.async;

import com.aol.cyclops.types.futurestream.Continuation;

public interface ContinuationStrategy {

	public void addContinuation(Continuation c);
	public void handleContinuation();
}
