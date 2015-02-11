package com.aol.simple.react.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * A class that can accept input streams and generate output streams where data sent in the Topic is guaranteed to be
 * provided to all Topic subsribers
 * 
 * @author johnmcclean
 *
 * @param <T> Data type for the Topic
 */
public class Topic<T> implements Adapter<T> {
	
	
	private final List<Queue<T>> queues= new ArrayList<Queue<T>>();
	private final DistributingCollection<T> distributor = new DistributingCollection<T>();

	/**
	 * Construct a new Topic
	 */
	public Topic() {
		Queue<T> q = new Queue<T>();
		queues.add(q);
		distributor.getTargets().add(q.getQueue());	
	}
	
	/**
	 * Construct a Topic using the Queue provided
	 * @param q Queue to back this Topic with
	 */
	public Topic(Queue<T> q) {
		queues.add(q);
		distributor.getTargets().add(q.getQueue());	
	}
	
	

	/**
	 * @param stream Input data from provided Stream
	 */
	public boolean fromStream(Stream<T> stream){
		stream.collect(Collectors.toCollection(()->distributor));
		return true;
		
	}
	
	/**
	 * @return Stream of CompletableFutures that can be used as input into a SimpleReact concurrent dataflow
	 */
	public Stream<CompletableFuture<T>> streamCompletableFutures(){
		Queue<T> q = new Queue<>();
		queues.add(q);
		distributor.getTargets().add(q.getQueue());
		return q.streamCompletableFutures();
		
	}
	
	/**
	 * @return Stream of data
	 */
	public Stream<T> stream(){
		Queue<T> q = new Queue<>();
		queues.add(q);
		distributor.getTargets().add(q.getQueue());
		return q.stream();
		
	}
	
	
	/**
	 * Close this Topic
	 * 
	 * @return true if closed
	 */
	public boolean close() {
		queues.forEach(it -> it.close());
		return true;
		
	}
	
	/**
	 * Add a single datapoint to this Queue
	 * 
	 * @param data data to add
	 * @return self
	 */
	@Override
	public T offer(T data) {
		fromStream(Stream.of(data));
		return data;
		
	}
	
	private static class DistributingCollection<T> extends ArrayList<T>{
		

		private static final long serialVersionUID = 1L;
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
