package com.aol.simple.react.async;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.Getter;


public class Signal<T> {

	private volatile T value;
	@Getter
	private final Queue<T> continuous = new Queue(new LinkedBlockingQueue());
	@Getter
	private final Queue<T> discrete = new Queue(new LinkedBlockingQueue());

	public T set(T newValue){
		continuous.add(newValue);
		
		swap(value,newValue);
		return newValue;
	}

	private synchronized void swap(T value2, T newValue) {
		if(!Objects.equals(value,newValue)){
			discrete.add(newValue);
			value = newValue;
		}
		
	}

	public void close(){
		continuous.setOpen(false);
		discrete.setOpen(false);
	}
	
}
