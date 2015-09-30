package com.aol.simple.react.stream.traits.future.operators;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.traits.BlockingStreamHelper;
import com.aol.simple.react.stream.traits.LazyFutureStream;

@AllArgsConstructor
public class OperationsOnFuturesImpl<T> implements OperationsOnFutures<T> {
	private final LazyFutureStream<T> lfs;
	@Override
	public LazyFutureStream<T> fromStreamOfFutures(Stream<FastFuture<T>> stream) {
		return lfs.fromStreamOfFutures(stream);
	}

	@Override
	public LazyStreamWrapper<T> getLastActive() {
		return lfs.getLastActive();
	}

	@Override
	public LazyFutureStream<T> withLastActive(LazyStreamWrapper<T> active) {
		return lfs.withLastActive(active);
	}

	@Override
	public T safeJoin(FastFuture<T> f) {
		return (T)BlockingStreamHelper.getSafe(f,lfs.getErrorHandler());
	}

	@Override
	public Continueable getSubscription() {
		return lfs.getSubscription();
	}

}
