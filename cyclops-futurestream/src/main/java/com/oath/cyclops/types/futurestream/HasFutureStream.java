package com.oath.cyclops.types.futurestream;

import cyclops.futurestream.FutureStream;

public interface HasFutureStream<T> {
    FutureStream<T> getStream();
}
