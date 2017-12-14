package com.oath.cyclops.types.futurestream;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.async.adapters.QueueFactory;
import com.oath.cyclops.internal.react.stream.ReactBuilder;


public interface ConfigurableStream<T, C> {

    ConfigurableStream<T, C> withTaskExecutor(Executor e);



    ConfigurableStream<T, C> withQueueFactory(QueueFactory<T> queue);

    ConfigurableStream<T, C> withErrorHandler(Optional<Consumer<Throwable>> errorHandler);

    ConfigurableStream<T, C> withSubscription(Continueable sub);

    ConfigurableStream<T, C> withAsync(boolean b);

    abstract Executor getTaskExecutor();



    ReactBuilder getSimpleReact();

    Optional<Consumer<Throwable>> getErrorHandler();

    boolean isAsync();

}
