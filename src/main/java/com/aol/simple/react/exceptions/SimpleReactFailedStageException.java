package com.aol.simple.react.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class SimpleReactFailedStageException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private final Object value;
	@Getter
	private final Throwable cause;
	
	public<T>  T getValue(){
		return (T)value;
	}
}
