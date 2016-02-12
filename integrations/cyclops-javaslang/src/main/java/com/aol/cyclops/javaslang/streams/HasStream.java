package com.aol.cyclops.javaslang.streams;

import com.aol.cyclops.control.SequenceM;

public interface HasStream<T> {
	SequenceM<T> getStream();
}
