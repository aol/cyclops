package com.aol.cyclops.types.stream;

/**
 * A HotStream (Stream already emitting data) that can be paused and unpaused
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the Stream
 */
public interface PausableHotStream<T> extends HotStream<T> {
    /**
     * Unpause this HotStream (restart data production)
     */
    void unpause();

    /**
     * Pause this HotStream (stop it producing data until unpaused)
     */
    void pause();
}
