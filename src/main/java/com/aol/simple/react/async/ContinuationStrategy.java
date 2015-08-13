package com.aol.simple.react.async;

import com.aol.simple.react.stream.traits.Continuation;

public interface ContinuationStrategy {

	public void addContinuation(Continuation c);
	public void handleContinuation();
}
