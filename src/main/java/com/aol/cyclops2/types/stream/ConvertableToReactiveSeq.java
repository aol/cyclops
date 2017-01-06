package com.aol.cyclops2.types.stream;

import cyclops.stream.ReactiveSeq;

/**
 * 
 * Represents a data type that is convertable to a ReactiveSeq
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this convertable type
 */
public interface ConvertableToReactiveSeq<T> {
    /**
     * @return ReactiveSeq generated from this convertable type
     */
    ReactiveSeq<T> reactiveSeq();
}
