package com.aol.cyclops.react;

import lombok.AllArgsConstructor;
import lombok.Builder;

import org.pcollections.PStack;

/**
 * Class that returned to blocking predicates for short circuiting result collection
 * 
 * @author johnmcclean
 *
 * @param <T> Result type
 */
@AllArgsConstructor
@Builder
public class Status<T> {
	
	private final int completed;
	private final int errors;
	private final int total;
	private final long elapsedNanos;
	private final PStack<T> resultsSoFar;

	public final int getAllCompleted(){
		return completed + errors;
	}
	
	public final long getElapsedMillis(){
		return elapsedNanos / 1000000;
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

	public PStack<T> getResultsSoFar() {
		return resultsSoFar;
	}
}
