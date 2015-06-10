package com.aol.simple.react.stream.traits;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.StreamWrapper;
import com.nurkiewicz.asyncretry.RetryExecutor;

public interface ConfigurableStream<T> { 

	ConfigurableStream<T> withTaskExecutor(Executor e);
	ConfigurableStream<T> withRetrier(RetryExecutor retry);
	ConfigurableStream<T> withWaitStrategy(Consumer<CompletableFuture> c);
	ConfigurableStream<T> withLazyCollector(LazyResultConsumer<T> lazy);
	ConfigurableStream<T> withQueueFactory(QueueFactory<T> queue);
	ConfigurableStream<T> withLastActive(StreamWrapper streamWrapper);
	ConfigurableStream<T>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	ConfigurableStream<T> withSubscription(Continueable sub);
	ConfigurableStream<T> withAsync(boolean b);
	abstract StreamWrapper getLastActive();
	abstract Executor getTaskExecutor();
	abstract RetryExecutor getRetrier();
	abstract boolean isEager();
	abstract BaseSimpleReact getSimpleReact();
	Optional<Consumer<Throwable>> getErrorHandler();
	boolean isAsync();
	
	static interface SimpleReactConfigurableStream<U> extends ConfigurableStream<U>{
		SimpleReactStream<U> withTaskExecutor(Executor e);
		SimpleReactStream<U> withRetrier(RetryExecutor retry);
		SimpleReactStream<U> withWaitStrategy(Consumer<CompletableFuture> c);
		SimpleReactStream<U> withLazyCollector(LazyResultConsumer<U> lazy);
		SimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);
		SimpleReactStream<U> withLastActive(StreamWrapper streamWrapper);
		SimpleReactStream<U> withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
		SimpleReactStream<U> withSubscription(Continueable sub);
		SimpleReactStream<U> withAsync(boolean b);
	}
}
