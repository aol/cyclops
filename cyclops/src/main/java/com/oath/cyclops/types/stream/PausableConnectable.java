package com.oath.cyclops.types.stream;

/**
 * A Connectable (Stream already emitting data) that can be paused and unpaused
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the Stream
 */
public interface PausableConnectable<T> extends Connectable<T> {
    /**
     * Unpause this Connectable (restart data production)
     */
    void unpause();

    /**
     * Pause this Connectable (stop it producing data until unpaused)
     */
    void pause();
}
