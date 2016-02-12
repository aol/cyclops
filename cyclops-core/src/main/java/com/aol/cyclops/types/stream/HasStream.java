package com.aol.cyclops.types.stream;

import com.aol.cyclops.sequence.SequenceM;

public interface HasStream<T> {
	SequenceM<T> getStream();
}
