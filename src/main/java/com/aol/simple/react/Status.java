package com.aol.simple.react;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
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

	public int getCompleted() {
		return completed;
	}

	public int getErrors() {
		return errors;
	}

	public int getTotal() {
		return total;
	}

	public long getElapsedNanos() {
		return elapsedNanos;
	}
}
