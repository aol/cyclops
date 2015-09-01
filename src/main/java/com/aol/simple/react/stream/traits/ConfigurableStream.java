package com.aol.simple.react.stream.traits;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.BaseSimpleReact;
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
	BaseSimpleReact getSimpleReact();
	Optional<Consumer<Throwable>> getErrorHandler();
	boolean isAsync();
	
	
	
}
