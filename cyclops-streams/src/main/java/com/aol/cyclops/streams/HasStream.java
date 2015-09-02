package com.aol.cyclops.streams;

import com.aol.cyclops.sequence.SequenceM;

public interface HasStream<T> {
	SequenceM<T> getStream();
}
