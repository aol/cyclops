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

import com.aol.simple.react.SimpleReactProcessingException;


public class Queue<T> implements Adapter<T>{

	
	private volatile boolean open=true;
	
	@Getter(AccessLevel.PACKAGE)
	private final BlockingQueue<T> queue;
	
	public Queue(){
		this(new LinkedBlockingQueue<>());
	}
	public Queue(BlockingQueue<T> queue){
		this.queue = queue;
	}
	
	public Stream<T> provideStream(){
		
		return Stream.generate(  ()->  ensureOpen()).flatMap(it -> it.stream());
	}
	
	public Stream<CompletableFuture<T>> provideStreamCompletableFutures(){
		return provideStream().map(it -> CompletableFuture.<T>completedFuture(it));
	}
	public void fromStream(Stream<T> stream){
		stream.collect(Collectors.toCollection(()->queue));
	}
	
	private Collection<T> ensureOpen(){
		Collection<T> data= new ArrayList();
		queue.drainTo(data);
		if(!open)
			throw new ClosedQueueException();
		return data;
	}
	public static class ClosedQueueException extends SimpleReactProcessingException{
		private static final long serialVersionUID = 1L;
	}
	@Override
	public T add(T data) {
		this.queue.add(data);
		return data;
	}
	@Override
	public void close() {
		this.open = false;
		
	}
}
