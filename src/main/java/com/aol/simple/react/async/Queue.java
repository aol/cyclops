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
public class Queue<T> implements Adapter<T> {

	private volatile boolean open = true;

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
	}

	/**
	 * @return Sequential Infinite (until Queue is closed) Stream of data from
	 *         this Queue
	 * 
	 *         use queue.stream().parallel() to convert to a parallel Stream
	 * 
	 */
	public Stream<T> stream() {

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

	private final Object closeLock = new Object();

	private Collection<T> ensureOpen() {

		if (!open)
			throw new ClosedQueueException();

		Collection<T> data = new ArrayList<>();
		queue.drainTo(data);

		return data;

	}

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

		return true;
	}
}
