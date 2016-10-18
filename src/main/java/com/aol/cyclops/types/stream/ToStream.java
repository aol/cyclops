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

public interface ToStream<T> extends Iterable<T>, ConvertableToReactiveSeq<T> {

    default LazyFutureStream<T> futureStream(final LazyReact react) {
        return react.fromIterable(this);
    }

    @Override
    default ReactiveSeq<T> reactiveSeq() {
        return ReactiveSeq.fromStream(StreamSupport.stream(getStreamable().spliterator(), false));
    }

    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
    }

    default Iterable<T> getStreamable() {
        return this;
    }

    default ReactiveSeq<T> reveresedStream() {
        return ReactiveSeq.fromStream(reveresedStream());
    }

    /**
     * @return SequenceM from this Streamable
     */
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(StreamSupport.stream(getStreamable().spliterator(), false));
    }

    default Stream<T> reveresedJDKStream() {
        final Iterable<T> streamable = getStreamable();
        if (streamable instanceof List) {
            return StreamSupport.stream(new ReversedIterator(
                                                             (List) streamable).spliterator(),
                                        false);
        }

        return SeqUtils.reverse(jdkStream());
    }

    default boolean isEmpty() {

        return this.reactiveSeq()
                   .isEmpty();
    }

    default Stream<T> jdkStream() {
        return StreamSupport.stream(getStreamable().spliterator(), false);

    }

}
