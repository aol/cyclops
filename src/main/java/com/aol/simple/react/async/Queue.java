package com.aol.simple.react.async;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Wither;

import org.jooq.lambda.Seq;

import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.traits.Continuation;
import com.aol.simple.react.util.SimpleTimer;
import com.google.common.collect.Lists;

/**
 * Inspired by scalaz-streams async.Queue (functionally similar, but Blocking)
 * 
 * A Queue that takes data from one or more input Streams and provides them to
 * one or more output Streams
 * 
 * @author johnmcclean, thomas kountis
 *
 * @param <T>
 *            Type of data stored in Queue
 */
@Wither
@AllArgsConstructor
public class Queue<T> implements Adapter<T> {

	private final static PoisonPill POISON_PILL = new PoisonPill();
	private final static PoisonPill CLEAR_PILL = new PoisonPill();
	private final ExceptionSoftener softener = ExceptionSoftener.singleton.factory
			.getInstance();
	private volatile boolean open = true;
	private final AtomicInteger listeningStreams = new AtomicInteger();
	private final int timeout;
	private final TimeUnit timeUnit;
	
	private final int offerTimeout;
	private final TimeUnit offerTimeUnit;
	private final int maxPoisonPills;

	@Getter(AccessLevel.PACKAGE)
	private final BlockingQueue<T> queue;
	
	@Getter
	private final Signal<Integer> sizeSignal;
	
	@Setter
	private  volatile Continuation continuation;
	private volatile boolean shuttingDown = false;


	/**
	 * Construct a Queue backed by a LinkedBlockingQueue
	 */
	public Queue() {
		this(new LinkedBlockingQueue<>());
	}
	
	Queue(BlockingQueue<T> queue,Signal<Integer> sizeSignal) {
		this.queue = queue;
		timeout = -1;
		timeUnit = TimeUnit.MILLISECONDS;
		maxPoisonPills = 90000;
		offerTimeout= Integer.MAX_VALUE;
		offerTimeUnit = TimeUnit.DAYS;
		this.sizeSignal = sizeSignal;
	}
	/**
	 * @param queue
	 *            BlockingQueue to back this Queue
	 */
	public Queue(BlockingQueue<T> queue) {
		this(queue,Signal.queueBackedSignal());
	}

	/**
	 * @return Sequential Infinite (until Queue is closed) Stream of data from
	 *         this Queue
	 * 
	 *         use queue.stream().parallel() to convert to a parallel Stream
	 * 
	 */
	public Seq<T> stream() {
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return Seq.seq(closingStream(this::ensureOpen,new AlwaysContinue()));
	}
	public Seq<T> stream(Continueable s) {
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return Seq.seq(closingStream(this::ensureOpen,s));
	}

	public Seq<Collection<T>> streamBatch(Continueable s,Function<Supplier<T>,Supplier<Collection<T>>> batcher) {
		
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return Seq.seq(closingStreamBatch(batcher.apply(this::ensureOpen),s));
	}
	public Seq<T> streamControl(Continueable s,Function<Supplier<T>,Supplier<T>> batcher) {
		
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return Seq.seq(closingStream(batcher.apply(this::ensureOpen),s));
	}

	private Stream<Collection<T>> closingStreamBatch(Supplier<Collection<T>> s, Continueable sub){
		
		Stream<Collection<T>> st = StreamSupport.stream(
	                new ClosingSpliterator(Long.MAX_VALUE, s,sub,this), false);
		
		 return st;
	}
	private Stream<T> closingStream(Supplier<T> s, Continueable sub){
		
		Stream<T> st = StreamSupport.stream(
	                new ClosingSpliterator(Long.MAX_VALUE, s,sub,this), false);
		
		 return st;
	}
	

	/**
	 * @return Infinite (until Queue is closed) Stream of CompletableFutures
	 *         that can be used as input into a SimpleReact concurrent dataflow
	 * 
	 *         This Stream itself is Sequential, SimpleReact will apply
	 *         concurrency / parralellism via the constituent CompletableFutures
	 * 
	 */
	public Seq<CompletableFuture<T>> streamCompletableFutures() {
		return stream().map(CompletableFuture::completedFuture);
	}

	/**
	 * @param stream
	 *            Input data from provided Stream
	 */
	public boolean fromStream(Stream<T> stream) {
		stream.collect(Collectors.toCollection(() -> queue));
		return true;
	}

	private T ensureOpen() {
		if(!open && queue.size()==0)
			throw new ClosedQueueException();
		
		T data = null;
		try {
			if(this.continuation!=null){
				while(open && queue.size()==0){
					continuation = continuation.proceed();
				}
				if(queue.size()>0)
					return (T)nillSafe(ensureNotPoisonPill(ensureClear(queue.poll())));
			}
			if(!open && queue.size()==0)
				throw new ClosedQueueException();
		
			if (timeout == -1)
				data = ensureClear(queue.take());
			else {
				data = ensureClear(queue.poll(timeout, timeUnit));
				if (data == null)
					throw new QueueTimeoutException();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			softener.throwSoftenedException(e);
		}
			
		ensureNotPoisonPill(data);
		if(sizeSignal!=null)
			this.sizeSignal.set(queue.size());

		return (T)nillSafe(data);
		
	}

	private T ensureClear(T poll) {
		if(CLEAR_PILL==poll){
			if(queue.size()>0)
				poll = ensureClear(queue.poll());
	
			this.queue.clear();
		}
	
		return poll;
	}

	private T ensureNotPoisonPill(T data) {
		if(data instanceof PoisonPill){
			throw new ClosedQueueException();
		
		}
		return data;
	}

	/**
	 * Exception thrown if Queue closed
	 * 
	 * @author johnmcclean
	 *
	 */
	@AllArgsConstructor
	
	public static class ClosedQueueException extends
			SimpleReactProcessingException {
		private static final long serialVersionUID = 1L;
		@Getter
		private final Object currentData;
		private final Object NOT_PRESENT = new Object();
		public ClosedQueueException() {
			currentData = NOT_PRESENT;
		}
		
		public boolean isDataPresent(){
			return currentData != NOT_PRESENT;
		}
	}

	/**
	 * Exception thrown if Queue polling timesout
	 * 
	 * @author johnmcclean
	 *
	 */
	public static class QueueTimeoutException extends
			SimpleReactProcessingException {
		private static final long serialVersionUID = 1L;
	}

	private static class PoisonPill { }


	/**
	 * Add a single data point to the queue
	 * 
	 * If the queue is a bounded queue and is full, will return false
	 * 
	 * @param data Data to add
	 * @return true if successfully added.
	 */
	public boolean add(T data){
	
		try{
			boolean result = queue.add((T)nullSafe(data));
			if(true){
				if(sizeSignal!=null)
					this.sizeSignal.set(queue.size());
			}
			return result;
			
		}catch(IllegalStateException e){
			return false;
		}
	}
	/**
	 * Offer a single datapoint to this Queue
	 * 
	 * If the queue is a bounded queue and is full it will block until space comes available or until
	 * offer time out is reached (default is Integer.MAX_VALUE DAYS).
	 * 
	 * @param data
	 *            data to add
	 * @return self
	 */
	@Override
	public boolean offer(T data) {
	
		if(!open)
			throw new ClosedQueueException();
		try {
			boolean result =  this.queue.offer((T)nullSafe(data),this.offerTimeout,this.offerTimeUnit);
			
			if(sizeSignal!=null)
				this.sizeSignal.set(queue.size());
			return result;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			this.softener.throwSoftenedException(e);
		}
		return false;
		
	}

	
	private boolean timeout(SimpleTimer timer) {
		
		if(timer.getElapsedNanoseconds()>=offerTimeUnit.toNanos(this.offerTimeout))
			return true;
		return false;
	}

	private Object nillSafe(T data) {
		
		if(NILL==data)
			return null;
		else
			return data;
	}
	private Object nullSafe(T data) {
		if(data==null)
			return NILL;
		else
			return data;
	}

	/**
	 * Close this Queue
	 * 
	 * @return true if closed
	 */
	@Override
	public boolean close() {
		this.open = false;
	
		if(this.queue.remainingCapacity()>0){
			for(int i=0;i<Math.min(maxPoisonPills, listeningStreams.get());i++){
				add((T)POISON_PILL);
			
			}
		}
		

		return true;
	}
	
	public void closeAndClear(){
	
		this.open = false;
		
		add((T)CLEAR_PILL);
	
	}
	
	public static final NIL NILL = new NIL();
	public static class NIL {}

	@AllArgsConstructor
	public static class QueueReader<T>{
		@Getter
		Queue<T> queue;
		public boolean notEmpty() {
			return queue.queue.size()!=0;
		}

		@Getter
		private volatile T last = null;
		private int size(){
			return queue.queue.size();
		}
		public T next(){
			last = queue.ensureOpen();
			return last;
		}
		public boolean isOpen() {
			return queue.open || notEmpty();
		}
		public Collection<T> drainToOrBlock() {
			Collection<T> result = Lists.newArrayList();
			if(size()>0)
				queue.queue.drainTo(result);
			else{
				try{
					result.add(queue.ensureOpen());
				}catch(ClosedQueueException e){
					queue.open=false;
					throw e;
				}
			}
			
			return result.stream().filter(it -> it!=POISON_PILL).collect(Collectors.toList());
		}
	}
	

	
}
