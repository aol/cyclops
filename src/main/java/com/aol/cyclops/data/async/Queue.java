package com.aol.cyclops.data.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.async.AdaptersModule.ClosingSpliterator;
import com.aol.cyclops.data.async.AdaptersModule.QueueToBlockingQueueWrapper;
import com.aol.cyclops.data.async.AdaptersModule.SingleContinuation;
import com.aol.cyclops.data.async.AdaptersModule.StreamOfContinuations;
import com.aol.cyclops.data.async.wait.DirectWaitStrategy;
import com.aol.cyclops.data.async.wait.WaitStrategy;
import com.aol.cyclops.internal.react.exceptions.SimpleReactProcessingException;
import com.aol.cyclops.react.async.subscription.AlwaysContinue;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.SimpleTimer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Wither;

/**
 * Inspired by scalaz-streams async.Queue (functionally similar, but Blocking)
 * 
 * A Queue that takes data from one or more input Streams and provides them to
 * one or more output Streams
 * 
 * Interface specifies a BlockingQueue, but NonBlockingQueues (such as ConcurrentLinkedQueue can be used
 * in conjunction with an implementation of the Continuation interface
 * @see QueueFactories#unboundedNonBlockingQueue() )
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
	
	private volatile boolean open = true;
	private final AtomicInteger listeningStreams = new AtomicInteger();
	private final int timeout;
	private final TimeUnit timeUnit;
	
	private final long offerTimeout;
	private final TimeUnit offerTimeUnit;
	private final int maxPoisonPills;

	@Getter(AccessLevel.PACKAGE)
	private final BlockingQueue<T> queue;
	private final WaitStrategy<T> consumerWait;
	private final WaitStrategy<T> producerWait;
	@Getter @Setter
	private volatile Signal<Integer> sizeSignal;
	
	private volatile Continueable sub;
	private ContinuationStrategy continuationStrategy;
	private volatile boolean shuttingDown = false;


	/**
	 * Construct a Queue backed by a LinkedBlockingQueue
	 */
	public Queue() {
		this(new LinkedBlockingQueue<>());
	}
	public Queue(QueueFactory<T> factory) {
       Queue<T> q = factory.build();
       this.queue = q.queue;
       timeout = q.timeout;
       timeUnit = q.timeUnit;
       maxPoisonPills = q.maxPoisonPills;
       offerTimeout= q.offerTimeout;
       offerTimeUnit = q.offerTimeUnit;
       
       this.consumerWait=q.consumerWait;
       this.producerWait=q.producerWait;
    }
	
	Queue(BlockingQueue<T> queue,WaitStrategy<T> consumer,WaitStrategy<T> producer) {
		this.queue = queue;
		timeout = -1;
		timeUnit = TimeUnit.MILLISECONDS;
		maxPoisonPills = 90000;
		offerTimeout= Integer.MAX_VALUE;
		offerTimeUnit = TimeUnit.DAYS;
		
		this.consumerWait=consumer;
		this.producerWait=producer;
	}
	/**
	 * Queue accepts a BlockingQueue to make use of Blocking semantics
	 *
	 * 
	 * @param queue
	 *            BlockingQueue to back this Queue
	 */
	public Queue(BlockingQueue<T> queue) {
		this(queue, new DirectWaitStrategy<T>(),new DirectWaitStrategy<T>());
	}
	Queue(BlockingQueue<T> queue, Signal<Integer>  sizeSignal) {
		this(queue, new DirectWaitStrategy<T>(),new DirectWaitStrategy<T>());
	}
	
	public Queue( java.util.Queue<T> q, WaitStrategy<T> consumer,WaitStrategy<T> producer){
		this(new QueueToBlockingQueueWrapper(q),consumer,producer);
	}
	

	public static<T> Queue<T> createMergeQueue(){
		Queue<T> q= new Queue<>();
		q.continuationStrategy=new StreamOfContinuations(q);
		return q;
	}
    
	/**
	 * @return Sequential Infinite (until Queue is closed) Stream of data from
	 *         this Queue
	 * <pre>
	 *         use queue.stream().parallel() to convert to a parallel Stream
	 * </pre>
	 */
	public ReactiveSeq<T> stream() {
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return ReactiveSeq.fromStream(closingStream(this::get,new AlwaysContinue()));
	}
	public ReactiveSeq<T> stream(Continueable s) {
		this.sub=s;
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return ReactiveSeq.fromStream(closingStream(this::get,s));
	}
	
	public ReactiveSeq<Collection<T>> streamBatchNoTimeout(Continueable s,Function<Supplier<T>,Supplier<Collection<T>>> batcher) {
		this.sub=s;
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return ReactiveSeq.fromStream(closingStreamBatch(batcher.apply(()->ensureOpen(this.timeout,this.timeUnit)),s));
	}
	public ReactiveSeq<Collection<T>> streamBatch(Continueable s,Function<BiFunction<Long,TimeUnit,T>,Supplier<Collection<T>>> batcher) {
		this.sub=s;
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return ReactiveSeq.fromStream(closingStreamBatch(batcher.apply((timeout,timeUnit)->ensureOpen(timeout,timeUnit)),s));
	}
	public ReactiveSeq<T> streamControl(Continueable s,Function<Supplier<T>,Supplier<T>> batcher) {
		
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return ReactiveSeq.fromStream(closingStream(batcher.apply(()->ensureOpen(this.timeout,this.timeUnit)),s));
	}
	public ReactiveSeq<CompletableFuture<T>> streamControlFutures(Continueable s,Function<Supplier<T>,CompletableFuture<T>> batcher) {
		this.sub=s;
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return ReactiveSeq.fromStream(closingStreamFutures(()->batcher.apply(()->ensureOpen(this.timeout,this.timeUnit)),s));
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
	private Stream<CompletableFuture<T>> closingStreamFutures(Supplier<CompletableFuture<T>> s, Continueable sub){
		
		Stream<CompletableFuture<T>> st = StreamSupport.stream(
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
	public ReactiveSeq<CompletableFuture<T>> streamCompletableFutures() {
		return stream().map(CompletableFuture::completedFuture);
	}

	/**
	 * @param stream
	 *            Input data from provided Stream
	 */
	public boolean fromStream(Stream<T> stream) {
		stream.peek(System.out::println).collect(Collectors.toCollection(() -> queue));
		return true;
	}
	private T ensureOpen(final long timeout, TimeUnit timeUnit) {
		if(!open && queue.size()==0)
			throw new ClosedQueueException();
		final  SimpleTimer timer = new SimpleTimer();
		final long timeoutNanos = timeUnit.toNanos(timeout);
		T data = null;
		try {
			if(this.continuationStrategy!=null){
				
				
				while(open && (data = ensureClear(queue.poll()))==null){
					
					this.continuationStrategy.handleContinuation();
					
					if(timeout!=-1)
						handleTimeout(timer,timeoutNanos);
					
				}
				if(data!=null)
					return (T)nillSafe(ensureNotPoisonPill(ensureClear(data)));
			}
			if(!open && queue.size()==0)
				throw new ClosedQueueException();
		
			if (timeout == -1){
				if(this.sub!=null && this.sub.timeLimit()>-1){
					data =ensureClear(consumerWait.take(()->queue.poll(sub.timeLimit(),TimeUnit.NANOSECONDS)));
					if (data == null)
						throw new QueueTimeoutException();
				}
				
				else
					data = ensureClear(consumerWait.take(()->queue.take()));
			}
			else {
				data = ensureClear(consumerWait.take(()->queue.poll(timeout, timeUnit)));
				if (data == null)
					throw new QueueTimeoutException();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw ExceptionSoftener.throwSoftenedException(e);
		}
			
		ensureNotPoisonPill(data);
		if(sizeSignal!=null)
			this.sizeSignal.set(queue.size());

		return (T)nillSafe(data);
		
	}

	private void handleTimeout(SimpleTimer timer, long timeout) {
		if(timer.getElapsedNanoseconds()>timeout){
			
			throw new QueueTimeoutException();
		}
		
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

		@Override
		public Throwable fillInStackTrace() {
			return this;
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
		@Override
		public Throwable fillInStackTrace() {
			
			return this;
		}

		private static final long serialVersionUID = 1L;
	}

	private static class PoisonPill { }


	public T poll(long time, TimeUnit unit) throws QueueTimeoutException{
		return this.ensureOpen(time, unit);
	}
	public T get(){
		
		return ensureOpen(this.timeout,this.timeUnit);
		
	}
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
			if(result){
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
			boolean result =  producerWait.offer(()->this.queue.offer((T)nullSafe(data),this.offerTimeout,this.offerTimeUnit));
			
			if(sizeSignal!=null)
				this.sizeSignal.set(queue.size());
			return result;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw ExceptionSoftener.throwSoftenedException(e);
		}
		
		
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
			
			last = queue.ensureOpen(queue.timeout,queue.timeUnit);
			
			return last;
		}
		public boolean isOpen() {
			return queue.open || notEmpty();
		}
		public Collection<T> drainToOrBlock() {
			
			Collection<T> result = new ArrayList<>();
			if(size()>0)
				queue.queue.drainTo(result);
			else{
				try{
					
					result.add(queue.ensureOpen(queue.timeout,queue.timeUnit));
					
					
				}catch(ClosedQueueException e){
				
					queue.open=false;
					throw e;
				}
			}
			
			return result.stream().filter(it -> it!=POISON_PILL).collect(Collectors.toList());
		}
	}


	public int size() {
		return queue.size();
	}
	public boolean isOpen() {
		return this.open;
	}
	
	public void addContinuation(Continuation c){
		if(this.continuationStrategy==null)
			continuationStrategy = new SingleContinuation(this);
		this.continuationStrategy.addContinuation(c);
	}
	
}
