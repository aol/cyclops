package com.aol.cyclops.types.futurestream;

import cyclops.stream.FutureStream;

public interface HasFutureStream<T> {
    FutureStream<T> getStream();
}
