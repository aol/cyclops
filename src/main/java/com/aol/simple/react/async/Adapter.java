package com.aol.simple.react.async;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface Adapter<T> {

	public T add(T data);
	public void fromStream(Stream<T> stream);
	public Stream<T> provideStream();
	public Stream<CompletableFuture<T>> provideStreamCompletableFutures();
	public void close();
}
