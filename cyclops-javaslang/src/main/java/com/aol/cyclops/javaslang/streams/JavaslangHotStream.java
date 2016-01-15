package com.aol.cyclops.javaslang.streams;

import java.util.Queue;
import java.util.function.Function;

import com.aol.cyclops.javaslang.sequence.ReactiveStream;


public interface JavaslangHotStream<T> {

	
		public ReactiveStream<T> connect();
		public ReactiveStream<T> connect(Queue<T> queue);
		public <R extends ReactiveStream<T>> R connectTo(Queue<T> queue,Function<ReactiveStream<T>,R> to);
}