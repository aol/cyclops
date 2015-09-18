package com.aol.simple.react.stream.lazy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Subscription;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.InfiniteClosingSpliterator;
import com.aol.simple.react.stream.InfiniteClosingSpliteratorFromIterator;
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
@ToString
@Slf4j 
public class LazyReact extends BaseSimpleReact {
	
	@Getter
	private final Executor executor;
	@Getter
	private final RetryExecutor retrier;
	
	
	
	private final Boolean async;
	@Getter
	private final MaxActive maxActive;
	@Getter
	private final Executor publisherExecutor;
	@Getter
	private final boolean streamOfFutures;
	@Getter
	private final boolean poolingActive;
	@Getter
	private final boolean autoOptimize;
	
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
		this.maxActive = MaxActive.IO;
		this.publisherExecutor=null;
		this.streamOfFutures=false;
		this.poolingActive=false;
		this.autoOptimize=true;
	}
	
	/**
	 * LazyReact builder with a new TaskExecutor with threads determined by threadPoolSize
	 * Max concurrent tasks is determined by concurrency
	 * 
	 * @param threadPoolSize
	 * @param maxActiveTasks
	 */
	public LazyReact(int threadPoolSize, int maxActiveTasks) {
		
		this.executor = Executors.newFixedThreadPool(threadPoolSize);
		this.retrier = new RetryBuilder().parallelism(threadPoolSize);
		this.async = true;
		this.maxActive = new MaxActive(maxActiveTasks,threadPoolSize);
		this.publisherExecutor=null;
		this.streamOfFutures=false;
		this.poolingActive=false;
		this.autoOptimize=true;
	}
	
	public <U> LazyFutureStream<U> from(CompletableFuture<U> cf){
		return this.construct(Stream.of(FastFuture.fromCompletableFuture(cf)));

	}
	public <U> LazyFutureStream<U> from(CompletableFuture<U>... cf){
		return (LazyFutureStream)this.construct(Stream.of(cf).map(FastFuture::fromCompletableFuture));

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
	public <U> LazyFutureStream<U> construct(Stream s) {
		this.log.debug("Constructing Stream with {}",this);
		return (LazyFutureStream) new LazyFutureStreamImpl<U>( this,s);

	}
	
	
	public <U> LazyFutureStream<U> constructFutures(
			Stream<CompletableFuture<U>> s) {
		LazyReact toUse = this.withStreamOfFutures(true);
		this.log.debug("Constructing Stream with {}",toUse);
		return toUse.construct((Stream)s);
	}
	
	/**
	 * Turn objectPooling on for any Streams created by the returned LazyReact builder
	 * This improves performance for Streams with very large numbers of elements, by reusing
	 * Future instances. By default Object Pooling is Off.
	 * <pre>
	 * {@code 
	 *  return new LazyReact()
						.objectPoolingOn()
						.range(0,5_000_000_000)
						.map(this::process)
						.forEach(System.out::println);
	   }
	   </pre>
	 * @return New LazyReact builder with Object pooling on.
	 */
	public LazyReact objectPoolingOn(){
		return this.withPoolingActive(true);
	}
	/**
	 * Turn objectPooling off for any Streams created by the returned LazyReact builder. By default Object Pooling is Off.
	 * 
	 * <pre>
	 * {@code 
	 * 	LazyReact react; 
	 *  
	 *    react.objectPoolingOff()
						.range(0,5_000)
						.map(this::process)
						.forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @return New LazyReact builder with Object pooling off.
	 */
	public LazyReact objectPoolingOff(){
		return this.withPoolingActive(false);
	}
	/**
	 * Turn on automatic threading optimization. Tasks will be 'fanned' out across threads initially
	 * and subsequent task completion events will trigger further processing on the same thread. Where
	 * operations require working on the results of multiple tasks, data will be forwarded to a Queue, data
	 * read from the queue will then also be 'fanned' out for processing across threads (with subsequent events
	 *  again occuring on the same thread). This is equivalent to optimal use of the async() and sync() operators
	 * on a Stream. autoOptimize overrides direct calls to sync() and async() on the Stream.
	 * By default autoOptimize is On.
	 * 
	 * <pre>
	 * {@code 
	 * new LazyReact().autoOptimizeOn()
	 *                  .range(0, 1_000_000)
						.map(i->i+2)
						.map(i->Thread.currentThread().getId())
						.peek(System.out::println)
						.runOnCurrent();
	 * }
	 * </pre>
	 * @return
	 */
	public LazyReact autoOptimizeOn(){
		return this.withAutoOptimize(true);
	}
	/**
	 * Turn off automatic threading management. This allows use async() and sync() to control fan out directly in a LazyFutureStream
	 * By default autoOptimize is On.
	 * 
	 *  <pre>
	 * {@code 
	 * 	LazyReact react; 
	 *  
	 *    react.autoOptimizeOff()
					    .range(0, 1_000_000)
						.map(i->i+2)
						.map(i->Thread.currentThread().getId())
						.peek(System.out::println)
						.runOnCurrent();
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	public LazyReact autoOptimizeOff(){
		return this.withAutoOptimize(false);
	}
	/**
	 * Start any created Streams in asyncrhonous mode - that is tasks will be submited to an Executor to be run.
	 * 
	 * @return LazyReact that creates Streams in async mode
	 */
	public LazyReact async(){
		return this.withAsync(true);
	}
	/**
	 * Start any created Streams in syncrhonous mode - that is tasks will be executed on the calling thread
	 * 
	 * @return LazyReact that creates Streams in sync mode
	 */
	public LazyReact sync(){
		return this.withAsync(false);
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
	
		return  constructFutures(stream);
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

		return (LazyFutureStream)reactI(actions);

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
		
		return construct( stream);
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

	

	
	

	@Override
	protected <U> LazyFutureStream<U> reactI(Supplier<U>... actions) {
		
		return constructFutures(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, this.getExecutor())));
	}
	/**
	 * @param executor Task Executor for concurrent tasks
	 * @param retrier Async Retrier
	 * @param async If true each task will be submitted to an executor service
	 */
	public LazyReact(Executor executor, RetryExecutor retrier,
			Boolean async, MaxActive maxActive, Executor pub,boolean streamOfFutures, 
			boolean objectPoolingActive,
			boolean autoOptimize) {
		super();
		this.executor = executor;
		this.retrier = retrier;
		this.async = Optional.ofNullable(async).orElse(true);
		this.maxActive = Optional.ofNullable(maxActive).orElse(MaxActive.IO);
		this.streamOfFutures = streamOfFutures;
		this.publisherExecutor=pub;
		this.poolingActive = objectPoolingActive;
		this.autoOptimize = autoOptimize;
	}

	public LazyReact(Executor currentThreadExecutor,
			AsyncRetryExecutor withScheduler, boolean async, MaxActive maxActive2) {
		this(currentThreadExecutor,withScheduler,async,maxActive2,null,false,false,async);
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
		
		return this.from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter.iterator(), Spliterator.ORDERED),false));
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
	
		return constructFutures(actions.map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())));
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
				.maxActive(MaxActive.CPU)
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


	
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow. Supplier will be executed multiple times sequentially / synchronously by populating thread.
	 * 
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> LazyFutureStream< U> reactInfinitely(final Supplier<U> s) {
		
		Subscription sub = new Subscription();
		LazyFutureStream stream = construct(StreamSupport.stream(
                new InfiniteClosingSpliterator(Long.MAX_VALUE, () -> s.get(),sub), false)).withSubscription(sub);
		
		return stream;
		

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
		
		Subscription sub = new Subscription();
		LazyFutureStream stream = constructFutures(StreamSupport.stream(
                new InfiniteClosingSpliterator(Long.MAX_VALUE, () -> CompletableFuture.supplyAsync(s),sub), false)).withSubscription(sub);
		
		return stream;
		

	}
	private static final Object NONE = new Object();
	/**
	 * Iterate infinitely using the supplied seed and function
	 * 
	 * @param seed Initial value
	 * @param f Function that performs the iteration
	 * @return Next stage in the flow / stream
	 */
	public <U> LazyFutureStream<U> iterateInfinitely(final U seed, final UnaryOperator<U> f){
		
		Subscription sub = new Subscription();
		 final Iterator<U> iterator = new Iterator<U> () {
	            @SuppressWarnings("unchecked")
	            U t = (U) NONE;

	            @Override
	            public boolean hasNext() {
	                return true;
	            }

	            @Override
	            public U  next() {
	                return t = (t == NONE) ? seed : f.apply(t);
	            }
	        };
	      return  construct(StreamSupport.stream(  new InfiniteClosingSpliteratorFromIterator(Long.MAX_VALUE,iterator,sub),false));

	}
	

}
