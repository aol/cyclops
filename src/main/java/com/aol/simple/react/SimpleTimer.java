package com.aol.simple.react;


/**
 * 
 * Simple Timer class that returns elapsed milliseconds since construction
 * 
 * @author johnmcclean
 *
 */
public final class SimpleTimer {

	private final long startMilliseconds =System.currentTimeMillis();
	
	
	/**
	 * @return Time elapsed in milliseconds since object construction
	 */
	public final long getElapsedMilliseconds() {
		return System.currentTimeMillis() - startMilliseconds;
	}
}

