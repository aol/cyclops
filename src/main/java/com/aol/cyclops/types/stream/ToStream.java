package com.aol.cyclops.types.stream;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.internal.stream.ReversedIterator;
import com.aol.cyclops.internal.stream.SeqUtils;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

/**
 * Interface that represents a data type that can be converted to a Stream
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the this ToStream type
 */
public interface ToStream<T> extends Iterable<T>, ConvertableToReactiveSeq<T> {

    /**
     * Convert this type to a LazyFutureStream using the provided LazyReact futureStream builder
     * to configure parallelism / executors and more.
     * 
     * @param react LazyReact futureStream builder (configurer)
     * @return This convertable type converted to a LazyFutureStream
     */
    default LazyFutureStream<T> futureStream(final LazyReact react) {
        return react.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.ConvertableToReactiveSeq#reactiveSeq()
     */
    @Override
    default ReactiveSeq<T> reactiveSeq() {
        return ReactiveSeq.fromStream(StreamSupport.stream(getStreamable().spliterator(), false));
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
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
        return ReactiveSeq.fromStream(StreamSupport.stream(getStreamable().spliterator(), false));
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
     * @return True if this type is empty, false otherwise
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
