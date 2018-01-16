package com.oath.cyclops.types.stream;

import cyclops.reactive.ReactiveSeq;

/**
 *
 * Represents a data type that is convertable to a ReactiveSeq
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this convertable type
 */
//@TODO replace only with ToStream
public interface ConvertableToReactiveSeq<T> {
    /**
     * @return ReactiveSeq generated from this convertable type
     */
    ReactiveSeq<T> reactiveSeq();
}
