package com.aol.simple.react.stream.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.nurkiewicz.asyncretry.RetryExecutor;

public interface Configuration<T> {

	SimpleReactStream<T> withTaskExecutor(ExecutorService e);
	SimpleReactStream<T> withRetrier(RetryExecutor retry);
	SimpleReactStream<T> withWaitStrategy(Consumer<CompletableFuture> c);
	SimpleReactStream<T> withEager(boolean eager);
	SimpleReactStream<T> withLazyCollector(LazyResultConsumer<T> lazy);
	SimpleReactStream<T> withQueueFactory(QueueFactory<T> queue);
}
