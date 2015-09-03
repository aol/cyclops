package com.aol.cyclops.sequence;

import java.util.stream.Stream;

import com.aol.cyclops.streams.StreamUtils;


public class SequenceMFactoryImpl implements SequenceMFactory {

	public <T> SequenceM<T> sequenceM(Stream<T> s, Object data){
		return StreamUtils.sequenceM(s,data);
	}
}
