package com.aol.cyclops.internal.stream;

import com.aol.cyclops.util.stream.Streamable;

import lombok.AllArgsConstructor;
import lombok.Getter;



@AllArgsConstructor
public  class StreamableImpl<T> implements Streamable<T>{
	@Getter
	private final Iterable<T> streamable;

	
	
}