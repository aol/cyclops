package com.aol.simple.react.stream.simple;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import com.aol.simple.react.stream.FutureStreamImpl;
import com.nurkiewicz.asyncretry.RetryExecutor;


public class SimpleReactStreamImpl<U> extends FutureStreamImpl<U>{
	SimpleReactStreamImpl(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor, final RetryExecutor retrier,boolean isEager) {
		super(stream,executor,retrier,isEager);
	}
}
