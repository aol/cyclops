package com.aol.simple.react.async;

import com.aol.simple.react.stream.traits.Continuation;

public class SingleContinuation implements ContinuationStrategy {
	private final Queue<?> queue;
	private  Continuation continuation= null;
	
	public SingleContinuation(Queue<?> queue){
		this.queue = queue;
	}
	
	@Override
	public void addContinuation(Continuation c) {
		continuation = c;

	}

	@Override
	public void handleContinuation(){
		
			continuation = continuation.proceed();
			
	}

}
