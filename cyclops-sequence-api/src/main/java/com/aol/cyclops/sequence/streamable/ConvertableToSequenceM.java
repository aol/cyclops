package com.aol.cyclops.sequence.streamable;

import com.aol.cyclops.sequence.SequenceM;

public interface ConvertableToSequenceM<T> {
	SequenceM<T> sequenceM();
}
