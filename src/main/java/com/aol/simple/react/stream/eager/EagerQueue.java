package com.aol.simple.react.stream.eager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.simple.react.async.Queue;

public class EagerQueue<T> {
	private final static Executor service = Executors.newSingleThreadExecutor();
	Queue<T> queue;
	int size;
	
	public Seq<CompletableFuture<T>> stream() {
		return Seq.seq(Stream.generate(()->CompletableFuture.supplyAsync(()->queue.get(),service))
				.limit(size));
		
	}
 
}
