package com.aol.cyclops.types.stream;

import com.aol.cyclops.control.SequenceM;

public interface HasStream<T> {
	SequenceM<T> getStream();
}
