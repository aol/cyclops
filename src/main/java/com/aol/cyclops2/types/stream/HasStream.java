package com.aol.cyclops2.types.stream;

import cyclops.stream.ReactiveSeq;

/**
 * A type that lazy has a reactiveStream of data or can be converted to a Stream of data
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
