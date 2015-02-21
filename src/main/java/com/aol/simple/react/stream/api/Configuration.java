package com.aol.simple.react.stream.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.nurkiewicz.asyncretry.RetryExecutor;

public interface Configuration<T> {

	FutureStream<T> withTaskExecutor(ExecutorService e);
	FutureStream<T> withRetrier(RetryExecutor retry);
	FutureStream<T> withWaitStrategy(Consumer<CompletableFuture> c);
	FutureStream<T> withEager(boolean eager);
	FutureStream<T> withLazyCollector(LazyResultConsumer<T> lazy);
	FutureStream<T> withQueueFactory(QueueFactory<T> queue);
}
