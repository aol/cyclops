package com.aol.cyclops.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.reactive.ReactiveTask;

public class ReactiveSeqFutureOpterationsImpl<T> extends BaseFutureOperationsImpl<T> {

    public ReactiveSeqFutureOpterationsImpl(final Executor exec, final ReactiveSeq<T> stream) {
        super(exec, stream);

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return new ReactiveTask(
                                getExec(), FutureStreamUtils.forEachX(getStream(), numberOfElements, consumer)
                                                            .map2(r -> CompletableFuture.runAsync(r, getExec())));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError) {
        return new ReactiveTask(
                                getExec(), FutureStreamUtils.forEachXWithError(getStream(), numberOfElements, consumer, consumerError)
                                                            .map2(r -> CompletableFuture.runAsync(r, getExec())));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return new ReactiveTask(
                                getExec(), FutureStreamUtils.forEachXEvents(getStream(), numberOfElements, consumer, consumerError, onComplete)
                                                            .map2(r -> CompletableFuture.runAsync(r, getExec())));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachWithError(final Consumer<? super T> consumerElement,
            final Consumer<? super Throwable> consumerError) {

        return new ReactiveTask(
                                getExec(), FutureStreamUtils.forEachWithError(getStream(), consumerElement, consumerError)
                                                            .map2(r -> CompletableFuture.runAsync(r, getExec())));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        return new ReactiveTask(
                                getExec(), FutureStreamUtils.forEachEvent(getStream(), consumerElement, consumerError, onComplete)
                                                            .map2(r -> CompletableFuture.runAsync(r, getExec())));
    }
}
