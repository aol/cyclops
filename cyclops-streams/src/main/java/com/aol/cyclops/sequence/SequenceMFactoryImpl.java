package com.aol.cyclops.sequence;

import java.util.Optional;
import java.util.stream.Stream;

import com.aol.cyclops.sequence.spliterators.ReversableSpliterator;
import com.aol.cyclops.streams.StreamUtils;


public class SequenceMFactoryImpl implements SequenceMFactory {

	public <T> SequenceM<T> sequenceM(Stream<T> s,ReversableSpliterator reversable){
		return StreamUtils.sequenceM(s,Optional.ofNullable(reversable));
	}
}
