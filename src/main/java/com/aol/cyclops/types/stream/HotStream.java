package com.aol.cyclops.types.stream;

import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;

public interface HotStream<T> {
	public ReactiveSeq<T> connect();
	public ReactiveSeq<T> connect(Queue<T> queue);
	public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<ReactiveSeq<T>,R> to);
}
