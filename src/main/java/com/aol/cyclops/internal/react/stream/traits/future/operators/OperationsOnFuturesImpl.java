package com.aol.cyclops.internal.react.stream.traits.future.operators;

import java.util.stream.Stream;

import com.aol.cyclops.internal.react.async.future.FastFuture;
import com.aol.cyclops.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.BlockingStreamHelper;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.OperationsOnFutures;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OperationsOnFuturesImpl<T> implements OperationsOnFutures<T> {
    private final LazyFutureStream<T> lfs;

    @Override
    public LazyFutureStream<T> fromStreamOfFutures(final Stream<FastFuture<T>> stream) {
        return lfs.fromStreamOfFutures(stream);
    }

    @Override
    public LazyStreamWrapper<T> getLastActive() {
        return lfs.getLastActive();
    }

    @Override
    public LazyFutureStream<T> withLastActive(final LazyStreamWrapper<T> active) {
        return lfs.withLastActive(active);
    }

    @Override
    public T safeJoin(final FastFuture<T> f) {
        return (T) BlockingStreamHelper.getSafe(f, lfs.getErrorHandler());
    }

    @Override
    public Continueable getSubscription() {
        return lfs.getSubscription();
    }

}
