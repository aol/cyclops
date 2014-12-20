package com.aol.simple.react.generators;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@Wither(AccessLevel.PRIVATE)
public class SequentialGenerator<T> implements Generator<T>{
	private final int size;
	private final int offset;
	
	
	public Generator<T> offset(int offset){
		return withOffset(offset);
	}
	public  Stream<CompletableFuture<T>> generate(Supplier<T> s){
		return Stream.<CompletableFuture<T>>generate(() -> CompletableFuture.supplyAsync(s)).skip(offset).limit(size);
	}
}