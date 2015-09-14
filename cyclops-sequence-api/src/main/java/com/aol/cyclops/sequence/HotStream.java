package com.aol.cyclops.sequence;

import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

public interface HotStream<T> {
	public SequenceM<T> connect();
	public SequenceM<T> connect(Queue<T> queue);
	public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<SequenceM<T>,R> to);
}
