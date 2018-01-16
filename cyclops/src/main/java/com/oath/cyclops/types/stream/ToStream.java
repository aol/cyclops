package com.oath.cyclops.types.stream;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.oath.cyclops.internal.stream.ReversedIterator;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.internal.stream.SeqUtils;

/**
 * Interface that represents a data type that can be converted to a Stream
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the this ToStream type
 */
public interface ToStream<T> extends Iterable<T>, ConvertableToReactiveSeq<T> {


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.stream.ConvertableToReactiveSeq#reactiveSeq()
     */
    @Override
    default ReactiveSeq<T> reactiveSeq() {
        return ReactiveSeq.fromSpliterator(this.spliterator());
    }




    /**
     * @return This type as a reversed Stream
     */
    default ReactiveSeq<T> reveresedStream() {
        return ReactiveSeq.fromStream(reveresedStream());
    }



}
