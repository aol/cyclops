package com.aol.cyclops.functions.collections.extensions;


/**
 * 
 * Simple Timer class that returns elapsed milliseconds since construction
 * 
 * @author johnmcclean
 *
 */
public final class SimpleTimer {

	private final long startNanoSeconds =System.nanoTime();
	
	
	/**
	 * @return Time elapsed in nanoseconds since object construction
	 */
	public final long getElapsedNanoseconds() {
		return System.nanoTime() - startNanoSeconds;
	}
}