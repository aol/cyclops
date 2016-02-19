package com.aol.cyclops.types.futurestream;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.internal.react.stream.BaseSimpleReact;
import com.aol.cyclops.internal.react.stream.ReactBuilder;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.nurkiewicz.asyncretry.RetryExecutor;

public interface ConfigurableStream<T,C> { 

	ConfigurableStream<T,C> withTaskExecutor(Executor e);
	ConfigurableStream<T,C> withRetrier(RetryExecutor retry);
	ConfigurableStream<T,C> withQueueFactory(QueueFactory<T> queue);
	
	ConfigurableStream<T,C>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	ConfigurableStream<T,C> withSubscription(Continueable sub);
	ConfigurableStream<T,C> withAsync(boolean b);
	
	abstract Executor getTaskExecutor();
	abstract RetryExecutor getRetrier();
	ReactBuilder getSimpleReact();
	Optional<Consumer<Throwable>> getErrorHandler();
	boolean isAsync();
	
	
	
}
