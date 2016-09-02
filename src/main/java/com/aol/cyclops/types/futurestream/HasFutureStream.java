package com.aol.cyclops.types.futurestream;

public interface HasFutureStream<T> {
    LazyFutureStream<T> getStream();
}
