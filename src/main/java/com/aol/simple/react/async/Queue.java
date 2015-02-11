package com.aol.simple.react.async;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;

/**
 * Inspired by scalaz-streams async.Queue
 * 
 * A Queue that takes data from one or more input Streams and provides them to
 * one or more output Streams
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            Type of data stored in Queue
 */
@Wither
@AllArgsConstructor
public class Queue<T> implements Adapter<T> {

	private final static PoisonPill POISON_PILL = new PoisonPill();
	private final ExceptionSoftener softener = ExceptionSoftener.singleton.factory
			.getInstance();
	private volatile boolean open = true;
	private volatile AtomicInteger listeningStreams = new AtomicInteger();
	private final int timeout;
	private final TimeUnit timeUnit;
	private final int maxPoisonPills;

	@Getter(AccessLevel.PACKAGE)
	private final BlockingQueue<T> queue;

	/**
	 * Construct a Queue backed by a LinkedBlockingQueue
	 */
	public Queue() {
		this(new LinkedBlockingQueue<>());
	}

	/**
	 * @param queue
	 *            BlockingQueue to back this Queue
	 */
	public Queue(BlockingQueue<T> queue) {
		this.queue = queue;
		timeout = -1;
		timeUnit = TimeUnit.MILLISECONDS;
		maxPoisonPills = 90000;
	}

	/**
	 * @return Sequential Infinite (until Queue is closed) Stream of data from
	 *         this Queue
	 * 
	 *         use queue.stream().parallel() to convert to a parallel Stream
	 * 
	 */
	public Stream<T> stream() {
		
		listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
		return Stream.generate(() -> ensureOpen()).flatMap(it -> it.stream());
	}

	

	/**
	 * @return Infinite (until Queue is closed) Stream of CompletableFutures
	 *         that can be used as input into a SimpleReact concurrent dataflow
	 * 
	 *         This Stream itself is Sequential, SimpleReact will apply
	 *         concurrency / parralellism via the constituent CompletableFutures
	 * 
	 */
	public Stream<CompletableFuture<T>> streamCompletableFutures() {
		return stream().map(it -> CompletableFuture.<T> completedFuture(it));
	}

	/**
	 * @param stream
	 *            Input data from provided Stream
	 */
	public boolean fromStream(Stream<T> stream) {
		stream.collect(Collectors.toCollection(() -> queue));
		return true;
	}

	

	private  Collection<T>  ensureOpen() {
		
		if(!open)
			throw new ClosedQueueException();
		T data = null;
		try {
			if (timeout == -1)
				data = queue.take(); 
			else {

				T next = queue.poll(timeout, timeUnit);
				if (next != null)
					data = next;
				else
					throw new QueueTimeoutException();

			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			softener.throwSoftenedException(e);

		}
		if(data instanceof PoisonPill)
			throw new ClosedQueueException();
		return Arrays.asList(data);

	};
	
	
	
	
	

	/**
	 * Exception thrown if Queue closed
	 * 
	 * @author johnmcclean
	 *
	 */
	public static class ClosedQueueException extends
			SimpleReactProcessingException {
		private static final long serialVersionUID = 1L;
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

	private static class PoisonPill{
		
	}
	/**
	 * Add a single datapoint to this Queue
	 * 
	 * @param data
	 *            data to add
	 * @return self
	 */
	@Override
	public T add(T data) {
		this.queue.add(data);
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
		for(int i=0;i<Math.max(maxPoisonPills, listeningStreams.get());i++)
			queue.add((T)POISON_PILL);

		return true;
	}
}
