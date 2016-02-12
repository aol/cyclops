package com.aol.cyclops.types.sequence;

import com.aol.cyclops.sequence.SequenceM;

public interface ConvertableToSequenceM<T> {
	SequenceM<T> sequenceM();
}
