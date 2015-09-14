package com.aol.cyclops.sequence;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HeadAndTail<T> {
	private final T head;
	private final SequenceM<T> tail;

	public T head() {
		return head;
	}

	public SequenceM<T> tail() {
		return tail;
	}
	
	public Stream<T> tailStream(){
		return tail().stream();
	}
}