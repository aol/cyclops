package com.aol.cyclops.javaslang.streams;

import java.util.Queue;
import java.util.function.Function;

import javaslang.collection.Stream;


public interface JavaslangHotStream<T> {

	
		public Stream<T> connect();
		public Stream<T> connect(Queue<T> queue);
		public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<Stream<T>,R> to);
}