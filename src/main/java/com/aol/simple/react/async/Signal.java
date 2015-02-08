package com.aol.simple.react.async;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import lombok.Getter;


public class Signal<T> {

	private volatile T value;
	@Getter
	private final Adapter<T> continuous;
	@Getter
	private final Adapter<T> discrete;
	
	public Signal(Adapter<T> continuous, Adapter<T> discrete) {
		super();
		this.continuous = continuous;
		this.discrete = discrete;
	}
	
	public static <T> Signal<T> queueBackedSignal(){
		return new Signal(new Queue<>(),new Queue<>());
	}
	public static <T> Signal<T> topicBackedSignal(){
		return new Signal(new Topic<>(),new Topic<>());
	}
	
	public void fromStream(Stream<T> stream){
		stream.forEach(next -> set(next));
	}
	
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
		continuous.close();
		discrete.close();
	}

	
	
}
