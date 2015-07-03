package com.aol.cyclops.streams;

import lombok.AllArgsConstructor;

import com.aol.cyclops.lambda.monads.SequenceM;

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
}