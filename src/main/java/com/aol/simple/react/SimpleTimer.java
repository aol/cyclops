package com.aol.simple.react;


/**
 * 
 * Simple Timer class that returns elapsed milliseconds since construction
 * 
 * @author johnmcclean
 *
 */
public final class SimpleTimer {

	private final long startMilliseconds =System.nanoTime();
	
	
	/**
	 * @return Time elapsed in nanoseconds since object construction
	 */
	public final long getElapsedNanoseconds() {
		return System.nanoTime() - startMilliseconds;
	}
}

