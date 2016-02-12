package com.aol.cyclops.types.stream;

import com.aol.cyclops.sequence.SequenceM;

public interface ConvertableToSequenceM<T> {
	SequenceM<T> sequenceM();
}
