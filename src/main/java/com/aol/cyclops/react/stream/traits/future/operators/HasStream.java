package com.aol.cyclops.react.stream.traits.future.operators;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public interface HasStream<T> {
	LazyFutureStream<T> getStream();
}
