package com.aol.simple.react.stream.traits.future.operators;

import com.aol.simple.react.stream.traits.LazyFutureStream;

public interface HasStream<T> {
	LazyFutureStream<T> getStream();
}
