package com.aol.simple.react;


public final class SimpleTimer {

	private final long startMilliseconds;
	

	public  SimpleTimer() {
		startMilliseconds = System.currentTimeMillis();
	}

	
	public final long getElapsedMilliseconds() {
		return System.currentTimeMillis() - startMilliseconds;
	}
}

