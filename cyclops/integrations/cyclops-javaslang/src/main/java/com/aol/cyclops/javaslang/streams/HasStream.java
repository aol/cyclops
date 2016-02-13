package com.aol.cyclops.javaslang.streams;

import com.aol.cyclops.control.ReactiveSeq;

public interface HasStream<T> {
	ReactiveSeq<T> getStream();
}
