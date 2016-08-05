package com.aol.cyclops.control;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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


import com.aol.cyclops.data.async.Adapter;

import org.reactivestreams.Publisher;


import com.aol.cyclops.internal.react.LazyFutureStreamImpl;
import com.aol.cyclops.internal.react.stream.InfiniteClosingSpliteratorFromSupplier;
import com.aol.cyclops.internal.react.stream.ReactBuilder;
import com.aol.cyclops.react.RetryBuilder;
import com.aol.cyclops.react.ThreadPools;
import com.aol.cyclops.react.async.subscription.Subscription;
import com.aol.cyclops.react.collectors.lazy.MaxActive;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
import com.aol.cyclops.util.function.Cacheable;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

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
public class LazyReact implements ReactBuilder {
	
	@Getter
	private final Executor executor;
	@Getter
	private final RetryExecutor retrier;
	
	
	
	private final Boolean async;
	@Getter
	private final MaxActive maxActive;
	
	
	@Getter
	private final boolean streamOfFutures;
	@Getter
	private final boolean poolingActive;
	@Getter
	private final boolean autoOptimize;
	@Getter
	private final boolean autoMemoize;
	@Getter
	private final Cacheable memoizeCache;
	
	
	
	public LazyReact autoMemoizeOn( Cacheable memoizeCache){
		return this.withAutoMemoize(true).withMemoizeCache(memoizeCache);
	}
	/* 
	 *	@return true if async
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#isAsync()
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
		
		this.streamOfFutures=false;
		this.poolingActive=false;
		this.autoOptimize=true;
		this.autoMemoize =false;
		this.memoizeCache=null;
	}
	public LazyReact(int maxActive,Executor executor) {
        
        this.executor = executor;
        this.retrier = null;
        this.async = true;
        this.maxActive = MaxActive.IO;
       
        this.streamOfFutures=false;
        this.poolingActive=false;
        this.autoOptimize=true;
        this.autoMemoize =false;
        this.memoizeCache=null;
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
	
		this.streamOfFutures=false;
		this.poolingActive=false;
		this.autoOptimize=true;
		this.autoMemoize =false;
		this.memoizeCache=null;
	}
	
	public <U> LazyFutureStream<U> from(CompletableFuture<U> cf){
		
		return this.constructFutures(Stream.of(cf));

	}
	public <U> LazyFutureStream<U> from(CompletableFuture<U>... cf){
		return (LazyFutureStream)this.constructFutures(Stream.of(cf));

	}
	
	/* 
	 * Construct a new Stream from another Stream
	 * 
	 *	@param s Stream to copy
	 *	@param org ignored for LazyFutureStreams
	 *	@return
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#construct(java.util.stream.Stream, java.util.List)
	 */
	public <U> LazyFutureStream<U> construct(Stream<U> s) {
		this.log.debug("Constructing Stream with {}",this);
		return (LazyFutureStream) new LazyFutureStreamImpl<U>( this,s);

	}
	
	
	public <U> LazyFutureStream<U> constructFutures(
			Stream<CompletableFuture<U>> s) {
		LazyReact toUse = this.withStreamOfFutures(true);
		this.log.debug("Constructing Stream with {}",toUse);
		return toUse.construct((Stream<U>)s);
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
	
	
	/**
     * Construct a LazyFutureStream from an Publisher
     * 
     * @param publisher
     *            to construct LazyFutureStream from
     * @return LazyFutureStream
     */
    public <T> LazyFutureStream<T> fromPublisher(Publisher<? extends T> publisher) {
        Objects.requireNonNull(publisher);
        SeqSubscriber<T> sub = SeqSubscriber.subscriber();
        publisher.subscribe(sub);
        return sub.toFutureStream(this);
    }
	/* 
	 * Generate an LazyFutureStream that is a range of Integers
	 * 
	 *	@param startInclusive Start of range 
	 *	@param endExclusive End of range
	 *	@return LazyFutureStream that is a range of Integers
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#range(int, int)
	 */
	public LazyFutureStream<Integer> range(int startInclusive, int endExclusive){
		return fromStream(ReactiveSeq.range(startInclusive, endExclusive));
	}

	/* 
	 * Construct a LazyFutureStream from the provided Stream of completableFutures
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return LazyFutureStream
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#fromStream(java.util.stream.Stream)
	 */
	public <U> LazyFutureStream<U> fromStreamFutures(
			Stream<CompletableFuture<U>> stream) {
	
		return  constructFutures(stream);
	}

	/* 
	 * Create a steam from provided Suppleirs
	 * 
	 *	@param actions Supplier Actions
	 *	@return
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#react(java.util.function.Supplier[])
	 */
	@SafeVarargs
	public final <U> LazyFutureStream<U> ofAsync(final Supplier<U>... actions) {

		return (LazyFutureStream)reactI(actions);

	}
	
	/* 
	 *  Construct a LazyFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return LazyFutureStream
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	public <U> LazyFutureStream<U> fromStream(Stream<U> stream) {
		
		return construct( stream);
	}



	/* 
	 * 
	 * Construct a LazyFutureStream from specified Suppliers. Each Supplier is executed asyncrhonously,
	 * and it's results provided to next phase of the Stream
	 * 
	 *	@param actions Suppliers to execute
	 *	@return LazyFutureStream
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#react(java.util.List)
	 */
	public <U> LazyFutureStream<U> react(Collection<Supplier<U>> actions) {
		
		ReactiveSeq<Supplier<U>> seq = actions instanceof List ? ReactiveSeq.fromList((List)actions) : ReactiveSeq.fromIterable(actions);
		return fromStreamAsync(seq);
	}
	
	@SafeVarargs
	private final <U> LazyFutureStream<U> reactI(Supplier<U>... actions) {
		
		return constructFutures(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, this.getExecutor())));
	}
	/**
	 * @param executor Task Executor for concurrent tasks
	 * @param retrier Async Retrier
	 * @param async If true each task will be submitted to an executor service
	 */
	public LazyReact(Executor executor, RetryExecutor retrier,
			Boolean async, MaxActive maxActive,boolean streamOfFutures, 
			boolean objectPoolingActive,
			boolean autoOptimize,
			boolean autoMemoize, Cacheable memoizeCache) {
		super();
		this.executor = executor;
		this.retrier = retrier;
		this.async = Optional.ofNullable(async).orElse(true);
		this.maxActive = Optional.ofNullable(maxActive).orElse(MaxActive.IO);
		this.streamOfFutures = streamOfFutures;
		
		this.poolingActive = objectPoolingActive;
		this.autoOptimize = autoOptimize;
		this.autoMemoize =autoMemoize;
		this.memoizeCache=memoizeCache;
	}

	public LazyReact(Executor currentThreadExecutor,
			AsyncRetryExecutor withScheduler, boolean async, MaxActive maxActive2) {
		this(currentThreadExecutor,withScheduler,async,maxActive2,false,
				false,async,false,null);
	}


	/* 
	 * Build an LazyFutureStream from the supplied iterable
	 * 
	 *	@param iter Iterable
	 *	@return LazyFutureStream
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#ofIterable(java.lang.Iterable)
	 */
	public <U> LazyFutureStream<U> fromIterable(Iterable<U> iter) {
		ReactiveSeq<U> seq = iter instanceof List ? ReactiveSeq.fromList((List)iter) : ReactiveSeq.fromIterable(iter);
		return this.fromStream(seq);
	}


	/* 
	 * Build an LazyFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Stream
	 * 
	 *	@param actions Stream to react to
	 *	@return LazyFutureStream
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#react(java.util.stream.Stream)
	 */
	public <U> LazyFutureStream<U> fromStreamAsync(Stream<? extends Supplier<U>> actions) {
	
		return constructFutures(actions.map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())));
	}

	/* 
	 * Build an LazyFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Iterator 
	 * 
	 *	@param actions Iterator to react to
	 *	@return LazyFutureStream
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#react(java.util.Iterator)
	 */
	public <U> LazyFutureStream<U> fromIteratorAsync(Iterator<? extends Supplier<U>> actions) {
		
		return this.<U>constructFutures(StreamSupport.<Supplier<U>>stream(Spliterators.<Supplier<U>>spliteratorUnknownSize(actions, Spliterator.ORDERED),false).map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())));
	}

	/*
	 * Build an LazyFutureStream that reacts Asynchronously to the Suppliers within the
	 * specified Iterator 
	 *   
	 *	@param actions
	 *	@return
	 * @see com.aol.cyclops.react.stream.BaseSimpleReact#reactIterable(java.lang.Iterable)
	 */
	public <U> LazyFutureStream<U> fromIterableAsync(Iterable<? extends Supplier<U>> actions) {
		ReactiveSeq<? extends Supplier<U>> seq = actions instanceof List ? ReactiveSeq.fromList((List)actions) : ReactiveSeq.fromIterable(actions);
		return this.<U>constructFutures(seq.map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())));
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


	
	
	
	
	
	private static final Object NONE = new Object();
	/**
	 * Iterate infinitely using the supplied seed and function
	 * Iteration is synchronized to support multiple threads using the same iterator.
	 * 
	 * @see LazyFutureStream#iterate for an alternative which does not synchronize iteration
	 * @param seed Initial value
	 * @param f Function that performs the iteration
	 * @return Next stage in the flow / stream
	 */
	public <U> LazyFutureStream<U> iterate(final U seed, final UnaryOperator<U> f){
		
		 Subscription sub = new Subscription();
		 final Supplier<U> supplier = new Supplier<U> () {
	            @SuppressWarnings("unchecked")
	            U t = (U) NONE;
 
	            @Override
	            public U  get() {
	                return t = (t == NONE) ? seed : f.apply(t);
	            }
	        };
	      return  construct(StreamSupport.<U>stream(  new InfiniteClosingSpliteratorFromSupplier<U>(Long.MAX_VALUE,supplier,sub),false));

	}
	
	public <U> LazyFutureStream<U> fromAdapter(Adapter<U> adapter){
	    Subscription sub = new Subscription();
	    return this.construct(adapter.stream(sub));
	}
	/**
	 * Generate an infinite Stream
	 * 
	 * <pre>
	 * {@code 
	 *  new LazyReact().generate(()->"hello")
                       .limit(5)
                       .reduce(Semigroups.stringConcat);
                       
        //Optional[hellohellohellohellohello]         
	 * 
	 * }</pre>
	 * 
	 * @param generate Supplier that generates stream input
	 * @return
	 */
	public <U> LazyFutureStream<U> generate(final Supplier<U> generate){
	   
	    return  construct(StreamSupport.<U>stream(  new InfiniteClosingSpliteratorFromSupplier<U>(Long.MAX_VALUE,generate,new Subscription()),false));
	}
	
	public <U> LazyFutureStream< U> generateAsync(final Supplier<U> s) {
        return this.constructFutures(ReactiveSeq.generate(()->1).map(n->CompletableFuture.supplyAsync(s,this.getExecutor())));
	    
    }
	/**
	 * Start a reactive flow from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> LazyFutureStream<U> from(final Iterator<U> iterator){
		return fromStream(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),false));
		
	
	}
	/**
	 * Start a reactive flow from a Collection using an Iterator
	 * 
	 * @param collection - Collection SimpleReact will iterate over at the start of the flow
	 *
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <R> LazyFutureStream<R> from(final Collection<R> collection){
		return fromStream(collection.stream());
	}


	

	
	
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public  LazyFutureStream<Integer> from(final IntStream stream) {
		
		return fromStream(stream.boxed());
	
	}
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public  LazyFutureStream<Double> from(final DoubleStream stream) {
		return fromStream(stream.boxed());
	}
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public  LazyFutureStream<Long> from(final LongStream stream) {
		return fromStream(stream.boxed());
	}
	@SafeVarargs
	public final <U> LazyFutureStream<U> of(U...array){
		return fromStream(Stream.of(array));
	}
	
	
	
	
}
