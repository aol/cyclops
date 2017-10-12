package com.aol.cyclops2.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.aol.cyclops2.internal.react.stream.traits.future.operators.LazyFutureStreamUtils;
import cyclops.reactive.FutureStream;
import com.aol.cyclops2.types.reactive.ReactiveStreamsTerminalFutureOperations;
import com.aol.cyclops2.types.reactive.ReactiveTask;

public class FutureOpterationsImpl<T> implements ReactiveStreamsTerminalFutureOperations<T> {
    private final Executor exec;
    private final FutureStream<T> stream;

    public FutureOpterationsImpl(final Executor exec, final FutureStream<T> stream) {
        this.exec = exec;
        this.stream = stream;

    }

   

    @Override
    public <X extends Throwable> ReactiveTask forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        return new ReactiveTask(
                                exec, LazyFutureStreamUtils.forEachX(stream, numberOfElements, consumer)
                                                                .map2(r -> CompletableFuture.runAsync(r, exec)));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError) {
        return new ReactiveTask(
                                exec, LazyFutureStreamUtils.forEachXWithError(stream, numberOfElements, consumer, consumerError)
                                                                .map2(r -> CompletableFuture.runAsync(r, exec)));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return new ReactiveTask(
                                exec, LazyFutureStreamUtils.forEachXEvents(stream, numberOfElements, consumer, consumerError, onComplete)
                                                                .map2(r -> CompletableFuture.runAsync(r, exec)));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEach(final Consumer<? super T> consumerElement,
                                                      final Consumer<? super Throwable> consumerError) {

        return new ReactiveTask(
                                exec, LazyFutureStreamUtils.forEachWithError(stream, consumerElement, consumerError)
                                                                .map2(r -> CompletableFuture.runAsync(r, exec)));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                                      final Runnable onComplete) {
        return new ReactiveTask(
                                exec, LazyFutureStreamUtils.forEachEvent(stream, consumerElement, consumerError, onComplete)
                                                                .map2(r -> CompletableFuture.runAsync(r, exec)));
    }
}
