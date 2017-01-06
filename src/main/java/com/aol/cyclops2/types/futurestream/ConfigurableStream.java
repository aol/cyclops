package com.aol.cyclops2.types.futurestream;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import cyclops.async.QueueFactory;
import com.aol.cyclops2.internal.react.stream.ReactBuilder;
import com.aol.cyclops2.react.async.subscription.Continueable;
import com.nurkiewicz.asyncretry.RetryExecutor;

public interface ConfigurableStream<T, C> {

    ConfigurableStream<T, C> withTaskExecutor(Executor e);

    ConfigurableStream<T, C> withRetrier(RetryExecutor retry);

    ConfigurableStream<T, C> withQueueFactory(QueueFactory<T> queue);

    ConfigurableStream<T, C> withErrorHandler(Optional<Consumer<Throwable>> errorHandler);

    ConfigurableStream<T, C> withSubscription(Continueable sub);

    ConfigurableStream<T, C> withAsync(boolean b);

    abstract Executor getTaskExecutor();

    abstract RetryExecutor getRetrier();

    ReactBuilder getSimpleReact();

    Optional<Consumer<Throwable>> getErrorHandler();

    boolean isAsync();

}
