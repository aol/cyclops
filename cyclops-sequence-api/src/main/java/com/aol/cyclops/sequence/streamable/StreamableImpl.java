package com.aol.cyclops.sequence.streamable;

import lombok.Getter;
import lombok.Value;



@Value
public  class StreamableImpl<T> implements Streamable<T>{
	@Getter
	private final T streamable;
	
}