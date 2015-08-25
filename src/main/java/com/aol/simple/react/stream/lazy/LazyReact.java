package com.aol.simple.react.stream.lazy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * 
 * Builder class for LazyFutureStreams
 *
 * 
 * react methods - submit Suppliers to task executor
 * of methods - build Streams directly from data 
 * 
 * @author johnmcclean
 *
 */

@Builder
@Wither
public class LazyReact extends BaseLazySimpleReact {
	
	@Getter
	private final Executor executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = false;
	
	
	private final Boolean async;
	@Getter
	private final MaxActive maxActive;
	@Getter
	private boolean streamOfFutures =false;
	
	/* 
	 *	@return true if async
	 * @see com.aol.simple.react.stream.BaseSimpleReact#isAsync()
	 */
	public boolean isAsync(){
		return async;
	}
	

	/**
	 * Construct a LazyReact builder using standard thread pool.
	 * By default, unless ThreadPools is configured otherwise this will be sized
	 * to the available processors
	 * 
	 * @see ThreadPools#getStandard()
	 */
	public LazyReact(){
		
		this( ThreadPools.getStandard());
		
	}
	/**
	 * Construct a LazyReact builder with provided Executor
	 * 
	 * @param executor Executor to use
	 */
	public LazyReact(Executor executor) {
		
		this.executor = executor;
		this.retrier = null;
		this.async = true;
		this.maxActive = MaxActive.defaultValue.factory.getInstance();
		
	}
	
	/**
	 * LazyReact builder with a new TaskExecutor with threads determined by threadPoolSize
	 * Max concurrent tasks is determined by concurrency
	 * 
	 * @param threadPoolSize
	 * @param concurrency
	 */
	public LazyReact(int threadPoolSize, int concurrency) {
		
		this.executor = Executors.newFixedThreadPool(threadPoolSize);
		this.retrier = new RetryBuilder().parallelism(threadPoolSize);
		this.async = true;
		this.maxActive = new MaxActive(concurrency,threadPoolSize);
		
	}
	
	public <U> LazyFutureStream<U> from(CompletableFuture<U> cf){
		return this.construct(Stream.of(FastFuture.fromCompletableFuture(cf)), Arrays.asList(FastFuture.fromCompletableFuture(cf)));

	}
	public <U> LazyFutureStream<U> from(CompletableFuture<U>... cf){
		return (LazyFutureStream)this.construct(Stream.of(cf).map(FastFuture::fromCompletableFuture), 
				(List)Stream.of(cf).map(FastFuture::fromCompletableFuture).collect(Collectors.toList()));

	}
	/* 
	 * Construct a new Stream from another Stream
	 * 
	 *	@param s Stream to copy
	 *	@param org ignored for LazyFutureStreams
	 *	@return
	 * @see com.aol.simple.react.stream.BaseSimpleReact#construct(java.util.stream.Stream, java.util.List)
	 */
	@Override
	public <U> LazyFutureStream<U> construct(Stream s,List<FastFuture> org) {
		
		return (LazyFutureStream) new LazyFutureStreamImpl<U>( this,s);

	}
	
	@Override
	public <U> LazyFutureStream<U> constructFutures(
			Stream<CompletableFuture<U>> s, List<FastFuture> org) {
		
		return this.withStreamOfFutures(true).construct((Stream)s, org);
	}
	/* 
	 * Generate an LazyFutureStream that is a range of Integers
	 * 
	 *	@param startInclusive Start of range 
	 *	@param endExclusive End of range
	 *	@return LazyFutureStream that is a range of Integers
	 * @see com.aol.simple.react.stream.BaseSimpleReact#range(int, int)
	 */
	@Override
	public LazyFutureStream<Integer> range(int startInclusive, int endExclusive){
		return from(IntStream.range(startInclusive, endExclusive));
	}

	/* 
	 * Construct a LazyFutureStream from the provided Stream of completableFutures
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStream(java.util.stream.Stream)
	 */
	@Override
	public <U> LazyFutureStream<U> fromStream(
			Stream<CompletableFuture<U>> stream) {
	
		return (LazyFutureStream)super.fromStream(stream);
	}

	/* 
	 * Create a steam from provided Suppleirs
	 * 
	 *	@param actions Supplier Actions
	 *	@return
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.function.Supplier[])
	 */
	@SafeVarargs
	public final <U> LazyFutureStream<U> react(final Supplier<U>... actions) {

		return (LazyFutureStream)super.reactI(actions);

	}
	
	/* 
	 *  Construct a LazyFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public <U> LazyFutureStream<U> from(Stream<U> stream) {
		
		return (LazyFutureStream)super.from(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public  LazyFutureStream<Integer> from(IntStream stream) {
		
		return (LazyFutureStream)super.from(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  LazyFutureStream<Double> from(DoubleStream stream) {
		
		return (LazyFutureStream)super.from(stream);
	}
	

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  LazyFutureStream<Long> from(LongStream stream) {
		
		return (LazyFutureStream)super.from(stream);
	}
	
	/* 
	 * Construct a LazyFutureStream from array
	 * 
	 *	@param array Array that forms basis of Stream
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#of(java.lang.Object[])
	 */
	@Override
	public <U> LazyFutureStream<U> of(U... array) {
		
		return (LazyFutureStream)super.of(array);
	}

	/* 
	 * 
	 * Construct a LazyFutureStream from specified Suppliers. Each Supplier is executed asyncrhonously,
	 * and it's results provided to next phase of the Stream
	 * 
	 *	@param actions Suppliers to execute
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.List)
	 */
	@Override
	public <U> LazyFutureStream<U> react(Collection<Supplier<U>> actions) {
		
		return (LazyFutureStream)super.react(actions);
	}

	/**
	 * Start a LazyFutureStream from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <U> LazyFutureStream<U> from(Iterator<U> iterator) {
		
		return (LazyFutureStream)super.from(iterator);
	}

	/**
	 * Start a LazyFutureStream from a Collection
	 * 
	 * @param collection - Collection Stream will be formed from
	 *
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <R> LazyFutureStream<R> from(Collection<R> collection) {
		
		return (LazyFutureStream)super.from(collection);
	}

	

	/**
	 * Generate an infinite LazyFutureStream
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	@Override
	public <U> LazyFutureStream<U> reactInfinitely(Supplier<U> s) {
		
		return (LazyFutureStream)super.reactInfinitely(s);
	}
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow. Supplier may be executed multiple times in parallel asynchronously by populating thread.
	 * Active CompletableFutures may grow rapidly.
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> LazyFutureStream< U> reactInfinitelyAsync(final Supplier<U> s) {
		return (LazyFutureStream<U>)super.reactInfinitelyAsync(s);
		

	}
	/* 
	 * Create an Infinite LazyFutureStream using provided params
	 * 
	 *	@param seed Initial value
	 *	@param f Generates subsequent values for the stream
	 *	@return Infinite LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#iterateInfinitely(java.lang.Object, java.util.function.UnaryOperator)
	 */
	@Override
	public <U> LazyFutureStream<U> iterateInfinitely(U seed, UnaryOperator<U> f) {
	
		return (LazyFutureStream)super.iterateInfinitely(seed, f);
	}

	

	@Override
	protected <U> LazyFutureStream<U> reactI(Supplier<U>... actions) {
		
		return (LazyFutureStream)super.reactI(actions);
	}
	/**
	 * @param executor Task Executor for concurrent tasks
	 * @param retrier Async Retrier
	 * @param async If true each task will be submitted to an executor service
	 */
	public LazyReact(Executor executor, RetryExecutor retrier,
			Boolean async, MaxActive maxActive, boolean streamOfFutures) {
		super();
		this.executor = executor;
		this.retrier = retrier;
		this.async = Optional.ofNullable(async).orElse(true);
		this.maxActive = Optional.ofNullable(maxActive).orElse(MaxActive.defaultValue.factory.getInstance());
		this.streamOfFutures = streamOfFutures;
	}

	public LazyReact(Executor currentThreadExecutor,
			AsyncRetryExecutor withScheduler, boolean b, MaxActive maxActive2) {
		this(currentThreadExecutor,withScheduler,b,maxActive2,false);
	}


	/* 
	 * Build an LazyFutureStream from the supplied iterable
	 * 
	 *	@param iter Iterable
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#ofIterable(java.lang.Iterable)
	 */
	@Override
	public <U> LazyFutureStream<U> fromIterable(Iterable<U> iter) {
		
		return (LazyFutureStream)super.of(iter);
	}


	/* 
	 * Build an LazyFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Stream
	 * 
	 *	@param actions Stream to react to
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.stream.Stream)
	 */
	@Override
	public <U> LazyFutureStream<U> react(Stream<Supplier<U>> actions) {
	
		return (LazyFutureStream)super.react(actions);
	}

	/* 
	 * Build an LazyFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Iterator 
	 * 
	 *	@param actions Iterator to react to
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.Iterator)
	 */
	@Override
	public <U> LazyFutureStream<U> react(Iterator<Supplier<U>> actions) {
		
		return (LazyFutureStream)super.react(actions);
	}

	/*
	 * Build an LazyFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Iterator 
	 *   
	 *	@param actions
	 *	@return
	 * @see com.aol.simple.react.stream.BaseSimpleReact#reactIterable(java.lang.Iterable)
	 */
	@Override
	public <U> LazyFutureStream<U> reactIterable(Iterable<Supplier<U>> actions) {
		
		return (LazyFutureStream)super.reactIterable(actions);
	}
	
	
	
	/**
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact parallelBuilder() {
		return new LazyReact();
	}

	/**
	 * Construct a new LazyReact builder, with a new task executor and retry
	 * executor with configured number of threads
	 * 
	 * @param parallelism
	 *            Number of threads task executor should have
	 * @return LazyReact instance
	 */
	public static LazyReact parallelBuilder(int parallelism) {
		return LazyReact.builder().executor(Executors.newFixedThreadPool(parallelism))
				.retrier(new RetryBuilder().parallelism(parallelism)).build();
	}

	/**
	 * @return new LazyReact builder configured with standard parallel executor
	 *         By default this is the ForkJoinPool common instance but is
	 *         configurable in the ThreadPools class
	 * 
	 * @see ThreadPools#getStandard() see RetryBuilder#getDefaultInstance()
	 */
	public static LazyReact parallelCommonBuilder() {
		return LazyReact
				.builder()
				.executor(ThreadPools.getStandard())
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								ThreadPools.getCommonFreeThreadRetry()))
				.build();
	}

	/**
	 * @return new LazyReact builder configured to run on a separate thread
	 *         (non-blocking current thread), sequentially New ForkJoinPool will
	 *         be created
	 */
	public static LazyReact sequentialBuilder() {
		return LazyReact
				.builder()
				.async(false)
				.executor(Executors.newFixedThreadPool(1))
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								Executors.newScheduledThreadPool(2))).build();
	}

	/**
	 * @return LazyReact builder configured to run on a separate thread
	 *         (non-blocking current thread), sequentially Common free thread
	 *         Executor from
	 */
	public static LazyReact sequentialCommonBuilder() {
		return LazyReact
				.builder()
				.async(false)
				.executor(ThreadPools.getCommonFreeThread())
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								ThreadPools.getCommonFreeThreadRetry()))
				.build();
	}
	/**
	 * @return LazyReact builder configured to run on a separate thread
	 *         (non-blocking current thread), sequentially Common free thread
	 *         Executor from
	 */
	public static LazyReact sequentialCurrentBuilder() {
		return LazyReact
				.builder()
				.async(false)
				.executor(ThreadPools.getCurrentThreadExecutor())
				.retrier(
						RetryBuilder.getDefaultInstance().withScheduler(
								ThreadPools.getCommonFreeThreadRetry()))
				.build();
	}


	
	

}
