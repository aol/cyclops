package com.aol.simple.react;

import lombok.AllArgsConstructor;

import com.google.common.collect.ImmutableList;

@AllArgsConstructor
public class Status<T> {
	private final int completed;
	private final int errors;
	private final int total;
	private final long elapsedNanos;
	private final ImmutableList<T> resultsSoFar;

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

	public ImmutableList<T> getResultsSoFar() {
		return resultsSoFar;
	}
}
