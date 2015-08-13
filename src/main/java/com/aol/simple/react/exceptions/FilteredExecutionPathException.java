package com.aol.simple.react.exceptions;

public class FilteredExecutionPathException extends
		SimpleReactProcessingException {

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	private static final long serialVersionUID = 1L;

	
}