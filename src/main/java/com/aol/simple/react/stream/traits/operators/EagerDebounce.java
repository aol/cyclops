package com.aol.simple.react.stream.traits.operators;

import java.util.Optional;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;

@AllArgsConstructor
public class EagerDebounce<T> implements Supplier<Optional<T>>{
	private volatile long last;
	private final Queue<T> queue;
	private final long  timeNanos;
	@Override
	public Optional<T> get() {
		
		Optional<T> result = Optional.empty();
		try {
			
			while(queue.isOpen() || queue.size()>0){
				
				result = Optional.of(queue.get());
				
				
				synchronized(queue)
				{
					if(System.nanoTime()-last>=timeNanos){
						last = System.nanoTime();
						return result;
					}else{
						result = Optional.empty();
					}
				}
				
			}
				
				
				
		} catch (ClosedQueueException e) {
			return Optional.empty();
		}
		return result;
	}
	
}
