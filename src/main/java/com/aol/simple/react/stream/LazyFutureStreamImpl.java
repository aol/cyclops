package com.aol.simple.react.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import com.nurkiewicz.asyncretry.RetryExecutor;

public class LazyFutureStreamImpl<U> extends FutureStreamImpl<U> implements
		LazyFutureStream<U> {
	
	LazyFutureStreamImpl(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor, final RetryExecutor retrier) {
		super(stream,executor,retrier,false);
	}
}
