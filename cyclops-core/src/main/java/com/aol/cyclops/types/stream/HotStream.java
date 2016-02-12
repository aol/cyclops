package com.aol.cyclops.types.stream;

import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.control.SequenceM;

public interface HotStream<T> {
	public SequenceM<T> connect();
	public SequenceM<T> connect(Queue<T> queue);
	public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<SequenceM<T>,R> to);
}
