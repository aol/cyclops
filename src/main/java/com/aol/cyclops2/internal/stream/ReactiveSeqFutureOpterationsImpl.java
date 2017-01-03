package com.aol.cyclops2.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.types.stream.reactive.ReactiveStreamsTerminalFutureOperations;
import com.aol.cyclops2.types.stream.reactive.ReactiveTask;

public class ReactiveSeqFutureOpterationsImpl<T> implements ReactiveStreamsTerminalFutureOperations<T> {
    private final Executor exec;
    private final Stream<T> stream;

    public ReactiveSeqFutureOpterationsImpl(final Executor exec, final ReactiveSeq<T> stream) {
        this.exec = exec;
        this.stream = stream;

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return new ReactiveTask(
                exec, FutureStreamUtils.forEachX(stream, numberOfElements, consumer)
                .map2(r -> CompletableFuture.runAsync(r, exec)));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
                                                                final Consumer<? super Throwable> consumerError) {
        return new ReactiveTask(
                exec, FutureStreamUtils.forEachXWithError(stream, numberOfElements, consumer, consumerError)
                .map2(r -> CompletableFuture.runAsync(r, exec)));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
                                                             final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return new ReactiveTask(
                exec, FutureStreamUtils.forEachXEvents(stream, numberOfElements, consumer, consumerError, onComplete)
                .map2(r -> CompletableFuture.runAsync(r, exec)));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachWithError(final Consumer<? super T> consumerElement,
                                                               final Consumer<? super Throwable> consumerError) {

        return new ReactiveTask(
                exec, FutureStreamUtils.forEachWithError(stream, consumerElement, consumerError)
                .map2(r -> CompletableFuture.runAsync(r, exec)));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                                           final Runnable onComplete) {
        return new ReactiveTask(
                exec, FutureStreamUtils.forEachEvent(stream, consumerElement, consumerError, onComplete)
                .map2(r -> CompletableFuture.runAsync(r, exec)));
    }
}