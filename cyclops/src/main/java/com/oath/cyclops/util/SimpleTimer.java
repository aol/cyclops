package com.oath.cyclops.util;

/**
 *
 * Simple Timer class that returns elapsed milliseconds since construction
 *
 * @author johnmcclean
 *
 */
public final class SimpleTimer {

    private final long startNanoSeconds = System.nanoTime();

    /**
     * @return Time elapsed in nanoseconds since object construction
     */
    public final long getElapsedNanoseconds() {
        return System.nanoTime() - startNanoSeconds;
    }

    public final long getElapsedMillis(){
        return (long)(getElapsedNanoseconds()/ 1e+6);
    }
}
