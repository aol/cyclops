package com.oath.cyclops.types.stream;

import cyclops.reactive.ReactiveSeq;

/**
 * Interface that represents a data type that can be converted to a Stream
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the this ToStream type
 */
public interface ToStream<T> extends Iterable<T> {



    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromSpliterator(this.spliterator());
    }




    /**
     * @return This type as a reversed Stream
     */
    default ReactiveSeq<T> reveresedStream() {
        return ReactiveSeq.fromStream(reveresedStream());
    }



}
