package com.aol.simple.react.stream.lazy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.FutureStreamImpl;
import com.aol.simple.react.stream.StreamWrapper;
import com.nurkiewicz.asyncretry.RetryExecutor;

public class LazyFutureStreamImpl<U> extends FutureStreamImpl<U> implements LazyFutureStream<U>{
	
	LazyFutureStreamImpl(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor, final RetryExecutor retrier) {
		super(stream,executor,retrier,false);
	}

	
	@Override
	public <R, A> R collect(Collector<? super U, A, R> collector) {
		return block(collector);
	}
	
    public LazyFutureStream<U> withTaskExecutor(ExecutorService e){
		return null;
	}
	public LazyFutureStream<U> withRetrier(RetryExecutor retry){
		return null;
	}
	public LazyFutureStream<U> withWaitStrategy(Consumer<CompletableFuture> c){
		return null;
	}
	public LazyFutureStream<U> withEager(boolean eager){
		return null;
	}
	public LazyFutureStream<U> withLazyCollector(LazyResultConsumer<U> lazy){
		return null;
	}
	public LazyFutureStream<U> withQueueFactory(QueueFactory<U> queue){
		return null;
	}
	public LazyFutureStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler){
		return null;
	}
	public LazyFutureStream<U> withLastActive(StreamWrapper streamWrapper){
		return null;
	}
	
}
