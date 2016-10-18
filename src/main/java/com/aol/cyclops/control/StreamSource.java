package com.aol.cyclops.control;

import java.util.Objects;
import java.util.stream.Stream;

import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.util.stream.pushable.MultipleStreamSource;
import com.aol.cyclops.util.stream.pushable.PushableLazyFutureStream;
import com.aol.cyclops.util.stream.pushable.PushableReactiveSeq;
import com.aol.cyclops.util.stream.pushable.PushableStream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Create Java 8 Streams that data can be pushed into
 * 
 * @author johnmcclean
 *
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamSource {

    private final int backPressureAfter;
    private final boolean backPressureOn;

    /**
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple() {
        return new MultipleStreamSource<T>(
                                           StreamSource.ofUnbounded()
                                                       .createQueue());
    }

    /**
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(final int backPressureAfter) {
        return new MultipleStreamSource<T>(
                                           StreamSource.of(backPressureAfter)
                                                       .createQueue());
    }

    /**
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(final QueueFactory<?> q) {
        Objects.requireNonNull(q);
        return new MultipleStreamSource<T>(
                                           StreamSource.of(q)
                                                       .createQueue());
    }

    public static StreamSource of(final QueueFactory<?> q) {
        Objects.requireNonNull(q);
        return new StreamSource() {
            @SuppressWarnings("unchecked")
            @Override
            <T> Queue<T> createQueue() {
                return (Queue<T>) q.build();

            }
        };
    }

    public static StreamSource ofUnbounded() {
        return new StreamSource();
    }

    public static StreamSource of(final int backPressureAfter) {
        if (backPressureAfter < 1)
            throw new IllegalArgumentException(
                                               "Can't apply back pressure after less than 1 event");
        return new StreamSource(
                                backPressureAfter, true);
    }

    <T> Queue<T> createQueue() {

        Queue q;
        if (!backPressureOn)
            q = QueueFactories.unboundedNonBlockingQueue()
                              .build();
        else
            q = QueueFactories.boundedQueue(backPressureAfter)
                              .build();
        return q;
    }

    private StreamSource() {

        backPressureAfter = Runtime.getRuntime()
                                   .availableProcessors();
        backPressureOn = false;
    }

    /**
     * Create a pushable LazyFutureStream using the supplied ReactPool
     * 
     * @param s ReactPool to use to create the Stream
     * @return a Tuple2 with a Queue&lt;T&gt; and LazyFutureStream&lt;T&gt; - add data to the Queue
     * to push it to the Stream
     */
    public <T> PushableLazyFutureStream<T> futureStream(final LazyReact s) {

        final Queue<T> q = createQueue();
        return new PushableLazyFutureStream<T>(
                                               q, s.fromStream(q.stream()));

    }

    /**
     * Create a LazyFutureStream. his will call LazyFutureStream#futureStream(Stream) which creates
     * a sequential LazyFutureStream
     * 
     * @param adapter Adapter to create a LazyFutureStream from
     * @return A LazyFutureStream that will accept values from the supplied adapter
     */
    public static <T> LazyFutureStream<T> futureStream(final Adapter<T> adapter, final LazyReact react) {

        return react.fromStream(adapter.stream());
    }

    /**
     * Create a pushable JDK 8 Stream
     * @return a Tuple2 with a Queue&lt;T&gt; and Stream&lt;T&gt; - add data to the Queue
     * to push it to the Stream
     */
    public <T> PushableStream<T> stream() {
        final Queue<T> q = createQueue();
        return new PushableStream<T>(
                                     q, q.stream());

    }

    /**
     * Create a pushable {@link PushableReactiveSeq}
     * 
     * @return a Tuple2 with a Queue&lt;T&gt; and Seq&lt;T&gt; - add data to the Queue
     * to push it to the Stream
     */
    public <T> PushableReactiveSeq<T> reactiveSeq() {
        final Queue<T> q = createQueue();
        return new PushableReactiveSeq<T>(
                                          q, q.stream());
    }

    /**
     * Create a JDK 8 Stream from the supplied Adapter
     * 
     * @param adapter Adapter to create a Steam from
     * @return Stream that will accept input from supplied adapter
     */
    public static <T> Stream<T> stream(final Adapter<T> adapter) {

        return adapter.stream();
    }

    /**
     * Create a pushable {@link ReactiveSeq}
     * 
     * @param adapter Adapter to create a Seq from
     * @return A Seq that will accept input from a supplied adapter
     */
    public static <T> ReactiveSeq<T> reactiveSeq(final Adapter<T> adapter) {

        return adapter.stream();
    }

}
