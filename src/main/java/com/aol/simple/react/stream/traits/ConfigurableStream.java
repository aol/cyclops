package com.aol.simple.react.stream.traits;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.StreamWrapper;
import com.nurkiewicz.asyncretry.RetryExecutor;

public interface ConfigurableStream<T> {

	SimpleReactStream<T> withTaskExecutor(ExecutorService e);
	SimpleReactStream<T> withRetrier(RetryExecutor retry);
	SimpleReactStream<T> withWaitStrategy(Consumer<CompletableFuture> c);
	SimpleReactStream<T> withEager(boolean eager);
	SimpleReactStream<T> withLazyCollector(LazyResultConsumer<T> lazy);
	SimpleReactStream<T> withQueueFactory(QueueFactory<T> queue);
	SimpleReactStream<T> withLastActive(StreamWrapper streamWrapper);
	SimpleReactStream<T>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	
	abstract StreamWrapper getLastActive();
	abstract ExecutorService getTaskExecutor();
	abstract RetryExecutor getRetrier();
	abstract boolean isEager();
	abstract BaseSimpleReact getSimpleReact();
	Optional<Consumer<Throwable>> getErrorHandler();
}
