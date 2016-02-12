package com.aol.cyclops.types.stream;

import com.aol.cyclops.control.ReactiveSeq;

public interface ConvertableToSequenceM<T> {
	ReactiveSeq<T> sequenceM();
}
