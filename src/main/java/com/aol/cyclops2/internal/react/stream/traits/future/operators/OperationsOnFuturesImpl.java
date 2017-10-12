package com.aol.cyclops2.internal.react.stream.traits.future.operators;

import java.util.stream.Stream;

import com.aol.cyclops2.internal.react.async.future.FastFuture;
import com.aol.cyclops2.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops2.react.async.subscription.Continueable;
import com.aol.cyclops2.types.futurestream.BlockingStreamHelper;
import cyclops.reactive.FutureStream;
import com.aol.cyclops2.types.futurestream.OperationsOnFutures;

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
