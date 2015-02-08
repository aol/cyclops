package com.aol.simple.react.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;


public class Queue<T> {

	@Setter
	private volatile boolean open=true;
	@Delegate
	@Getter(AccessLevel.PACKAGE)
	private final BlockingQueue<T> queue;
	public Queue(){
		this(new LinkedBlockingQueue<>());
	}
	public Queue(BlockingQueue<T> queue){
		this.queue = queue;
	}
	
	public Stream<T> dequeue(){
		
		return Stream.generate(  ()->  ensureOpen()).flatMap(it -> it.stream());
	}
	
	public Stream<CompletableFuture<T>> dequeueForSimpleReact(){
		return dequeue().map(it -> CompletableFuture.<T>completedFuture(it));
	}
	public void enqueue(Stream<T> stream){
		stream.collect(Collectors.toCollection(()->queue));
	}
	
	private Collection<T> ensureOpen(){
		Collection<T> data= new ArrayList();
		queue.drainTo(data);
		if(!open)
			throw new ClosedQueueException();
		return data;
	}
	public static class ClosedQueueException extends RuntimeException{
		
	}
}
