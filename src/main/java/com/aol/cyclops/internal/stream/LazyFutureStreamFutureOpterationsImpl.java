package com.aol.cyclops.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.aol.cyclops.internal.react.stream.traits.future.operators.LazyFutureStreamUtils;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.reactive.ReactiveTask;

public class LazyFutureStreamFutureOpterationsImpl<T> extends BaseFutureOperationsImpl<T> {

    public LazyFutureStreamFutureOpterationsImpl(final Executor exec, final LazyFutureStream<T> stream) {
        super(exec, stream);

    }

    @Override
    public LazyFutureStream<T> getStream() {
        return (LazyFutureStream<T>) super.getStream();
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return new ReactiveTask(
                                getExec(), LazyFutureStreamUtils.forEachX(getStream(), numberOfElements, consumer)
                                                                .map2(r -> CompletableFuture.runAsync(r, getExec())));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError) {
        return new ReactiveTask(
                                getExec(), LazyFutureStreamUtils.forEachXWithError(getStream(), numberOfElements, consumer, consumerError)
                                                                .map2(r -> CompletableFuture.runAsync(r, getExec())));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return new ReactiveTask(
                                getExec(), LazyFutureStreamUtils.forEachXEvents(getStream(), numberOfElements, consumer, consumerError, onComplete)
                                                                .map2(r -> CompletableFuture.runAsync(r, getExec())));

    }

    @Override
    public <X extends Throwable> ReactiveTask forEachWithError(final Consumer<? super T> consumerElement,
            final Consumer<? super Throwable> consumerError) {

        return new ReactiveTask(
                                getExec(), LazyFutureStreamUtils.forEachWithError(getStream(), consumerElement, consumerError)
                                                                .map2(r -> CompletableFuture.runAsync(r, getExec())));
    }

    @Override
    public <X extends Throwable> ReactiveTask forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        return new ReactiveTask(
                                getExec(), LazyFutureStreamUtils.forEachEvent(getStream(), consumerElement, consumerError, onComplete)
                                                                .map2(r -> CompletableFuture.runAsync(r, getExec())));
    }
}
