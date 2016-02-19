package com.aol.cyclops.internal.react.stream.traits.future.operators;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import com.aol.cyclops.internal.react.async.future.FastFuture;
import com.aol.cyclops.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.BlockingStreamHelper;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.OperationsOnFutures;

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
