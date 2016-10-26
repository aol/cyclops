package com.aol.cyclops.types.stream;

import com.aol.cyclops.control.ReactiveSeq;

/**
 * A type that either has a stream of data or can be converted to a Stream of data
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this HasStream
 */
public interface HasStream<T> {
    /**
     * @return Stream of elements
     */
    ReactiveSeq<T> getStream();
}
