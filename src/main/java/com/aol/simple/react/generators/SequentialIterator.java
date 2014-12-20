package com.aol.simple.react.generators;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;



@AllArgsConstructor(access=AccessLevel.PRIVATE)
@Wither(AccessLevel.PRIVATE)
public class SequentialIterator<T> implements ReactIterator<T>{
	private final T seed;
	private final int times;
	private final int offset;
	
	public SequentialIterator(T seed) {
		this.seed = seed;
		this.times =0;
		this.offset =0;
	}
	
	public  Stream<CompletableFuture<T>> iterate(Function<T,T> f){
		return Stream.<CompletableFuture<T>>iterate(CompletableFuture.completedFuture(seed),(CompletableFuture<T> in) -> in.thenApplyAsync(f)).skip(offset).limit(times);
	}

	@Override
	public ReactIterator<T> times(int times) {
		return withTimes(times);
	}

	@Override
	public ReactIterator<T> offset(int offset) {
		return withOffset(offset);
	}


	
}