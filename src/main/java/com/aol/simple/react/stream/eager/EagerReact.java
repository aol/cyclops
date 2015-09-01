package com.aol.simple.react.stream.eager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Builder class for EagerFutureStreams
 * 
 * react methods - submit Suppliers to task executor
 * of methods - build Streams directly from data
 * 
 * @author johnmcclean
 *
 */
@Builder
@Wither
public class EagerReact extends BaseSimpleReact{
	@Getter
	private final Executor executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = true;
	
	private final Boolean async;
	
	
	
	
	
	public EagerReact(Executor executor, RetryExecutor retrier,
			Boolean async) {
		super(ThreadPools.getQueueCopyExecutor());
		this.executor = executor;
		this.retrier = retrier;
		this.async = Optional.ofNullable(async).orElse(true);
	}
	public EagerReact(Executor executor, RetryExecutor retrier,
			Boolean async,Executor queueCopyExecutor) {
		super(queueCopyExecutor);
		this.executor = executor;
		this.retrier = retrier;
		this.async = Optional.ofNullable(async).orElse(true);
	}
	
	/**
	 * Construct a EagerReact builder using standard thread pool.
	 * By default, unless ThreadPools is configured otherwise this will be sized
	 * to the available processors
	 * 
	 * @see ThreadPools#getStandard()
	 */
	public EagerReact(){
		this( ThreadPools.getStandard());
		
	}
	/**
	 * Construct a EagerReact builder with provided Executor
	 * 
	 * @param executor Executor to use
	 */
	public EagerReact(Executor executor) {
		super(ThreadPools.getQueueCopyExecutor());
		this.executor = executor;
		this.retrier = new AsyncRetryExecutor(ThreadPools.getStandardRetry());
		this.async=true;
		
	}
	public EagerReact withQueueCopyExecutor(Executor queueCopyExecutor){
		return new EagerReact(this.executor,this.retrier,this.async,queueCopyExecutor);
	}
	
	
	@Override
	public <U> EagerFutureStream<U> construct(Stream s) {
		return (EagerFutureStream) new EagerFutureStreamImpl<U>( this,s);
	}
	public <U> EagerFutureStream<U> fromStreamAsync(final Stream<CompletableFuture<U>> stream) {

		Stream s = stream;
		return  construct( Stream.of()).fromStreamOfFutures(s);
	}
	/* 
	 * Construct a EagerFutureStream from the provided Stream of completableFutures
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStream(java.util.stream.Stream)
	 */
	@Override
	public <U> EagerFutureStream<U> fromStream(
			Stream<CompletableFuture<U>> stream) {
	
		return (EagerFutureStream)super.fromStream(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public <U> EagerFutureStream<U> from(Stream<U> stream) {
		
		return (EagerFutureStream)super.from(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public EagerFutureStream<Integer> from(IntStream stream) {
		
		return (EagerFutureStream)super.from(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  EagerFutureStream<Double> from(DoubleStream stream) {
		
		return (EagerFutureStream)super.from(stream);
	}
	

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  EagerFutureStream<Long> from(LongStream stream) {
		
		return (EagerFutureStream)super.from(stream);
	}
	/* 
	 * Construct a EagerFutureStream from array
	 * 
	 *	@param array Array that forms basis of Stream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#of(java.lang.Object[])
	 */
	@Override
	public <U> EagerFutureStream<U> of(U... array) {
		
		return (EagerFutureStream)super.of(array);
	}
	
	@SafeVarargs
	public final  <U> EagerFutureStream<U> react(final Supplier<U>... actions) {

		return (EagerFutureStream)super.reactI(actions);

	}
	
	/* 
	 * 
	 * Construct a EagerFutureStream from specified Suppliers. Each Supplier is executed asyncrhonously,
	 * and it's results provided to next phase of the Stream
	 * 
	 *	@param actions Suppliers to execute
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.List)
	 */

	@Override
	public <U> EagerFutureStream<U> react(Collection<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.react(actions);
	}
	
	
	/**
	 * Start a LazyFutureStream from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <U> EagerFutureStream<U> from(Iterator<U> iterator) {
		
		return (EagerFutureStream)super.from(iterator);
	}

	/**
	 * Start a EagerFutureStream from a Collection
	 * 
	 * @param collection - Collection Stream will be formed from
	 *
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <R> EagerFutureStream<R> from(Collection<R> collection) {
		
		return (EagerFutureStream)super.from(collection);
	}



	/* 
	 *	@return isAsync : if true all tasks go through supplied executor - if false
	 * all tasks executed on the completing thread
	 * @see com.aol.simple.react.stream.BaseSimpleReact#isAsync()
	 */
	public boolean isAsync(){
		return async;
	}

	/* 
	 * Generate an EagerFutureStream that is a range of Integers
	 * 
	 *	@param startInclusive Start of range 
	 *	@param endExclusive End of range
	 *	@return EagerFutureStream that is a range of Integers
	 * @see com.aol.simple.react.stream.BaseSimpleReact#range(int, int)
	 */
	@Override
	public EagerFutureStream<Integer> range(int startInclusive, int endExclusive) {
		
		return (EagerFutureStream)super.range(startInclusive, endExclusive);
	}

	/* 
	 * Build an EagerFutureStream from the supplied iterable
	 * 
	 *	@param iter Iterable
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#ofIterable(java.lang.Iterable)
	 */
	@Override
	public <U> EagerFutureStream<U> fromIterable(Iterable<U> iter) {
		
		return (EagerFutureStream)super.of(iter);
	}

	/* 
	 * Build an EagerFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Stream
	 * 
	 *	@param actions Stream to react to
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.stream.Stream)
	 */
	@Override
	public <U> EagerFutureStream<U> react(Stream<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.react(actions);
	}

	/* 
	 * Build an EagerFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Iterator 
	 * 
	 *	@param actions Iterator to react to
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.Iterator)
	 */
	@Override
	public <U> EagerFutureStream<U> react(Iterator<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.react(actions);
	}

	/*
	 * Build an EagerFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Iterator 
	 *   
	 *	@param actions
	 *	@return
	 * @see com.aol.simple.react.stream.BaseSimpleReact#reactIterable(java.lang.Iterable)
	 */
	@Override
	public <U> EagerFutureStream<U> reactIterable(Iterable<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.reactIterable(actions);
	}
	public <U> EagerFutureStream<U> from(CompletableFuture<U> cf){
		return this.construct(Stream.of(cf));
	}
	public <U> EagerFutureStream<U> from(CompletableFuture<U>... cf){
		return this.construct(Stream.of(cf));
	}
	/**
	 * @return EagerReact for handling finite streams
	 * @see SimpleReact#SimpleReact()
	 */
	public static EagerReact parallelBuilder() {
		return new EagerReact();
	}

	/**
	 * Construct a new EagerReact builder, with a new task executor and retry
	 * executor with configured number of threads
	 * 
	 * @param parallelism
	 *            Number of threads task executor should have
	 * @return eager EagerReact instance
	 */
	public static EagerReact parallelBuilder(int parallelism) {
		return EagerReact.builder().executor(new ForkJoinPool(parallelism)).retrier(new RetryBuilder().parallelism(parallelism)).build();
		
	}

	/**
	 * @return new EagerReact builder configured with standard parallel executor
	 *         By default this is the ForkJoinPool common instance but is
	 *         configurable in the ThreadPools class
	 * 
	 * @see ThreadPools#getStandard() see RetryBuilder#getDefaultInstance()
	 */
	public static EagerReact parallelCommonBuilder() {
		return EagerReact
				.builder()
				.async(true)
				.executor(ThreadPools.getStandard())
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								ThreadPools.getCommonFreeThreadRetry()))
				.build();
	}

	/**
	 * @return new eager EagerReact builder configured to run on a separate
	 *         thread (non-blocking current thread), sequentially New
	 *         ForkJoinPool will be created
	 */
	public static EagerReact sequentialBuilder() {
		return EagerReact
				.builder()
				.async(false)
				.executor(Executors.newFixedThreadPool(1))
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								Executors.newScheduledThreadPool(1))).build();
	}

	/**
	 * @return new EagerReact builder configured to run on a separate thread
	 *         (non-blocking current thread), sequentially Common free thread
	 *         Executor from
	 */
	public static EagerReact sequentialCommonBuilder() {
		return EagerReact
				.builder()
				.async(false)
				.executor(ThreadPools.getCommonFreeThread())
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								ThreadPools.getCommonFreeThreadRetry()))
				.build();
	}
	/**
	 * @return new EagerReact builder configured to run on a separate thread
	 *         (non-blocking current thread), sequentially Common free thread
	 *         Executor from
	 */
	public static EagerReact sequentialCurrentBuilder() {
		return EagerReact
				.builder()
				.async(false)
				.executor(ThreadPools.getCurrentThreadExecutor())
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								ThreadPools.getCommonFreeThreadRetry()))
				.build();
	}
	

}