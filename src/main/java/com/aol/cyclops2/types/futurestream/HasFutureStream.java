package com.aol.cyclops2.types.futurestream;

import cyclops.stream.FutureStream;

public interface HasFutureStream<T> {
    FutureStream<T> getStream();
}
