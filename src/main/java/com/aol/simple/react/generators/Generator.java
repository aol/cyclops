package com.aol.simple.react.generators;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Generator<T>{
	public  Stream<CompletableFuture<T>> generate(Supplier<T> s);

	public Generator<T> offset(int offset);
}