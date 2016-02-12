package com.aol.cyclops.types.stream;

import com.aol.cyclops.control.SequenceM;

public interface ConvertableToSequenceM<T> {
	SequenceM<T> sequenceM();
}
