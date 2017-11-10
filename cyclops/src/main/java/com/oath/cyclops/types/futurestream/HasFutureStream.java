package com.oath.cyclops.types.futurestream;

import cyclops.reactive.FutureStream;

public interface HasFutureStream<T> {
    FutureStream<T> getStream();
}
