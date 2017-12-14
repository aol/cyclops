package com.oath.cyclops.internal.react.stream.traits.future.operators;

import java.util.stream.Stream;

import com.oath.cyclops.internal.react.async.future.FastFuture;
import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;
import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.types.futurestream.BlockingStreamHelper;
import com.oath.cyclops.types.futurestream.OperationsOnFutures;
import cyclops.futurestream.FutureStream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OperationsOnFuturesImpl<T> implements OperationsOnFutures<T> {
    private final FutureStream<T> lfs;

    @Override
    public FutureStream<T> fromStreamOfFutures(final Stream<FastFuture<T>> stream) {
        return lfs.fromStreamOfFutures(stream);
    }

    @Override
    public LazyStreamWrapper<T> getLastActive() {
        return lfs.getLastActive();
    }

    @Override
    public FutureStream<T> withLastActive(final LazyStreamWrapper<T> active) {
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
