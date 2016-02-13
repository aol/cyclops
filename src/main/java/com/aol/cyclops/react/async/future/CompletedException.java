package com.aol.cyclops.react.async.future;

public class CompletedException extends RuntimeException{
    private final Object resut;
    
	public CompletedException(Object resut) {
		
		this.resut = resut;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
