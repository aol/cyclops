package com.aol.simple.react.stream.traits.future.operators;

import com.aol.simple.react.stream.traits.FutureStream;

public interface HasStream<T> {
	FutureStream<T> getStream();
}
