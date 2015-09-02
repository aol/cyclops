package com.aol.cyclops.sequence;

import java.util.stream.Stream;

import com.aol.cyclops.streams.StreamUtils;


public class SequenceMFactoryImpl {

	public <T> SequenceM<T> sequenceM(Stream<T> s){
		return StreamUtils.sequenceM(s);
	}
}
