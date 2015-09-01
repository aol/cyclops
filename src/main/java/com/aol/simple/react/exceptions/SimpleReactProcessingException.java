package com.aol.simple.react.exceptions;

public class SimpleReactProcessingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SimpleReactProcessingException() {
		super();
		
	}

	public SimpleReactProcessingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public SimpleReactProcessingException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public SimpleReactProcessingException(String message) {
		super(message);
		
	}

	public SimpleReactProcessingException(Throwable cause) {
		super(cause);
		
	}
	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
