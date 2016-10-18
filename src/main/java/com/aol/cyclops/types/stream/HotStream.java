package com.aol.cyclops.types.stream;

import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.control.ReactiveSeq;

public interface HotStream<T> {
    public default ReactiveSeq<T> connect() {
        return connect(new OneToOneConcurrentArrayQueue<T>(
                                                           256));
    }

    public ReactiveSeq<T> connect(Queue<T> queue);

    public default <R extends Stream<T>> R connectTo(final Queue<T> queue, final Function<ReactiveSeq<T>, R> to) {
        return to.apply(connect(queue));
    }
}
