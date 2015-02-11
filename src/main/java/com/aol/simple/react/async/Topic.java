package com.aol.simple.react.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * A class that can accept input streams and generate output streams where data sent in the Topic is guaranteed to be
 * provided to all Topic subsribers
 * 
 * @author johnmcclean
 *
 * @param <T> Data type for the Topic
 */
public class Topic<T> implements Adapter<T> {
	
	
	@Getter(AccessLevel.PACKAGE) @VisibleForTesting
	private final DistributingCollection<T> distributor = new DistributingCollection<T>();
	@Getter(AccessLevel.PACKAGE) @VisibleForTesting
	private volatile ImmutableMap<Stream,Queue<T>> streamToQueue = ImmutableMap.of();
	private final Object lock = new Object();
	private volatile int index=0;

	/**
	 * Construct a new Topic
	 */
	public Topic() {
		Queue<T> q = new Queue<T>();
		//queues.add(q);
		distributor.addQueue(q);	
	}
	
	/**
	 * Construct a Topic using the Queue provided
	 * @param q Queue to back this Topic with
	 */
	public Topic(Queue<T> q) {
		//queues.add(q);
		distributor.addQueue(q);		
	}
	
	
	
	@Synchronized("lock")
	public void disconnect(Stream<T> stream){
		
		distributor.removeQueue(streamToQueue.get(stream));
		Map<Stream,Queue<T>> mutable = new HashMap<>(streamToQueue);
		mutable.remove(stream);
		this.streamToQueue = ImmutableMap.copyOf(mutable);
		this.index--;
	}
	
	@Synchronized("lock")
	private<R> Stream<R> connect(Function<Queue<T>,Stream<R>> streamCreator){
		Queue<T> queue = this.getNextQueue();
		Stream<R> stream = streamCreator.apply(queue);
		Map<Stream,Queue<T>> mutable = new HashMap<>(streamToQueue);
		mutable.put(stream,queue);
		this.streamToQueue = ImmutableMap.copyOf(mutable);
		return stream;
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
		return connect(q -> q.streamCompletableFutures());
	}
	
	/**
	 * @return Stream of data
	 */
	public Stream<T> stream(){
		
		
		return connect(q -> q.stream());
		
	}

	private Queue<T> getNextQueue() {
	
		if(index >= this.distributor.getSubscribers().size()){
			
			this.distributor.addQueue(new Queue());
			
			
		}
		return this.distributor.getSubscribers().get(index++);
	}
	
	
	/**
	 * Close this Topic
	 * 
	 * @return true if closed
	 */
	public boolean close() {
		this.distributor.getSubscribers().forEach(it -> it.close());
		return true;
		
	}
	
	public Signal<Integer> getSizeSignal(){
		return this.distributor.getSubscribers().get(0).getSizeSignal();
	}
	
	/**
	 * Add a single datapoint to this Queue
	 * 
	 * @param data data to add
	 * @return self
	 */
	@Override
	public boolean offer(T data) {
		fromStream(Stream.of(data));
		return true;
		
	}
	
	static class DistributingCollection<T> extends ArrayList<T>{
		

		private static final long serialVersionUID = 1L;
		@Getter
		private volatile ImmutableList<Queue<T>> subscribers = ImmutableList.of();
		
		private final Object lock = new Object();
		
		@Synchronized("lock")
		public void addQueue(Queue<T> q){
			 subscribers = ImmutableList.copyOf(Iterables.concat(subscribers,Lists.newArrayList(q)));
		}
		
		@Synchronized("lock")
		public void removeQueue(Queue<T> q){
			 subscribers = ImmutableList.copyOf(Collections2.filter(subscribers, Predicates.not(Predicates.equalTo(q))));
		}
		
		@Override
		public boolean add(T e) {
			subscribers.forEach(it -> it.offer(e));
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			subscribers.forEach(it -> c.forEach(next -> it.offer(next)));
			return true;
		}

		
		
	}


	


	
}
