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
     * @see com.oath.cyclops.types.reactiveStream.ConvertableToReactiveSeq#reactiveSeq()
     */
    @Override
    default ReactiveSeq<T> reactiveSeq() {
        return ReactiveSeq.fromSpliterator(getStreamable().spliterator());
    }


    /**
     * @return This type narrowed to an Iterable
     */
    default Iterable<T> getStreamable() {
        return this;
    }

    /**
     * @return This type as a reversed Stream
     */
    default ReactiveSeq<T> reveresedStream() {
        return ReactiveSeq.fromStream(reveresedStream());
    }

    /**
     * @return ReactiveSeq from this Streamable
     */
    default ReactiveSeq<T> stream() {
        return reactiveSeq();
    }

    /**
     * @return This type as a reversed Stream
     */
    default Stream<T> reveresedJDKStream() {
        final Iterable<T> streamable = getStreamable();
        if (streamable instanceof List) {
            return StreamSupport.stream(new ReversedIterator(
                                                             (List) streamable).spliterator(),
                                        false);
        }

        return SeqUtils.reverse(jdkStream());
    }

    /**
     * @return True if this type is zero, false otherwise
     */
    default boolean isEmpty() {

        return this.reactiveSeq()
                   .isEmpty();
    }

    /**
     * @return This type converted to a JDK Stream
     */
    default Stream<T> jdkStream() {
        return StreamSupport.stream(getStreamable().spliterator(), false);

    }

}
