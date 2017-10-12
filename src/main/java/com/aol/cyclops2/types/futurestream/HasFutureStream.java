package com.aol.cyclops2.types.futurestream;

import cyclops.reactive.FutureStream;

public interface HasFutureStream<T> {
    FutureStream<T> getStream();
}
