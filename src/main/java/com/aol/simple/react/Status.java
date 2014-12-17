package com.aol.simple.react;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Status {
	private final int completed;
	private final int errors;
	private final int total;
	private final long elapsedNanos;

	public final int getAllCompleted(){
		return completed + errors;
	}
	
	public final long getElapsedMillis(){
		return elapsedNanos * 1000000;
	}
}
