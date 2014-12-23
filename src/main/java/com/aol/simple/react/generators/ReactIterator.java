package com.aol.simple.react.generators;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;



public interface ReactIterator<T>{
	public  Stream<CompletableFuture<T>> iterate(Function<T,T> s);
	public ReactIterator<T> times(int times);
	public ReactIterator<T> offset(int offset);
}