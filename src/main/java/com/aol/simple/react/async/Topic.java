package com.aol.simple.react.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

public class Topic<T> {
	
	
	private final List<Queue<T>> queues= new ArrayList<Queue<T>>();
	private final DistributingCollection<T> distributor = new DistributingCollection<T>();

	public Topic(Queue<T> q) {
		queues.add(q);
		distributor.getTargets().add(q.getQueue());	
	}
	public Topic() {
		Queue<T> q = new Queue<T>();
		queues.add(q);
		distributor.getTargets().add(q.getQueue());	
	}

	public void enqueue(Stream<T> stream){
		stream.collect(Collectors.toCollection(()->distributor));
		
	}
	public Stream<CompletableFuture<T>> dequeueForSimpleReact(){
		Queue<T> q = new Queue<>();
		queues.add(q);
		distributor.getTargets().add(q.getQueue());
		return q.dequeueForSimpleReact();
		
	}
	public Stream<T> dequeue(){
		Queue<T> q = new Queue<>();
		queues.add(q);
		distributor.getTargets().add(q.getQueue());
		return q.dequeue();
		
	}

	public void close() {
		queues.forEach(it -> it.setOpen(false));
		
	}
	
	
	static class DistributingCollection<T> extends ArrayList<T>{
		

		@Getter
		private final List<Collection<T>> targets = new ArrayList<>();
		
		
		@Override
		public boolean add(T e) {
			targets.forEach(it -> it.add(e));
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			targets.forEach(it -> it.addAll(c));
			return true;
		}

		
		
	}


	
}
