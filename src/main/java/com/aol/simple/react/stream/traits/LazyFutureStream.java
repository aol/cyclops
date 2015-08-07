package com.aol.simple.react.stream.traits;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Subscriber;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.reactivestreams.FutureStreamAsyncPublisher;
import com.aol.simple.react.reactivestreams.FutureStreamSynchronousPublisher;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyFutureStreamImpl;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */

public interface LazyFutureStream<U> extends  LazyStream<U>,FutureStream<U>, LazyToQueue<U>, FutureStreamAsyncPublisher<U>,FutureStreamSynchronousPublisher<U> {

	default void  subscribe(Subscriber<? super U> s){
		if(isAsync())
			FutureStreamAsyncPublisher.super.subscribe(s);
		else
			FutureStreamSynchronousPublisher.super.subscribe(s);
	}
	/* 
	 * Change task executor for the next stage of the Stream
	 * 
	 * <pre>
	 * {@code
	 *  LazyFutureStream.of(1,2,3,4)
	 *  					.map(this::loadFromDb)
	 *  					.withTaskExecutor(parallelBuilder().getExecutor())
	 *  					.map(this::processOnDifferentExecutor)
	 *  					.toList();
	 * }
	 * </pre>
	 * 
	 *	@param e New executor to use
	 *	@return Stream ready for next stage definition
	 * @see com.aol.simple.react.stream.traits.ConfigurableStream#withTaskExecutor(java.util.concurrent.Executor)
	 */
	LazyFutureStream<U> withTaskExecutor(Executor e);
	/* 
	 * Change the Retry Executor used in this stream for subsequent stages
	 * <pre>
	 * {@code
	 * List<String> result = new LazyReact().react(() -> 1)
				.withRetrier(executor)
				.capture(e -> error = e)
				.retry(serviceMock).block();
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 *	@param retry Retry executor to use
	 *	@return Stream 
	 * @see com.aol.simple.react.stream.traits.ConfigurableStream#withRetrier(com.nurkiewicz.asyncretry.RetryExecutor)
	 */
	LazyFutureStream<U> withRetrier(RetryExecutor retry);

	LazyFutureStream<U> withWaitStrategy(Consumer<CompletableFuture> c);

	//LazyFutureStream<U> withEager(boolean eager);

	LazyFutureStream<U> withLazyCollector(LazyResultConsumer<U> lazy);
	/* 
	 * Change the QueueFactory type for the next phase of the Stream.
	 * Default for EagerFutureStream is an unbounded blocking queue, but other types 
	 * will work fine for a subset of the tasks (e.g. an unbonunded non-blocking queue).
	 * 
	 * <pre>
	 * {@code
	 * List<Collection<String>> collected = LazyFutureStream
				.react(data)
				.withQueueFactory(QueueFactories.boundedQueue(1))
				.onePer(1, TimeUnit.SECONDS)
				.batchByTime(10, TimeUnit.SECONDS)
				.limit(15)
				.toList();
	 * }
	 * </pre>
	 *	@param queue Queue factory to use for subsequent stages
	 *	@return Stream
	 * @see com.aol.simple.react.stream.traits.ConfigurableStream#withQueueFactory(com.aol.simple.react.async.QueueFactory)
     * @see com.aol.simple.react.stream.traits.LazyFutureStream#unboundedWaitFree()
     * @see com.aol.simple.react.stream.traits.LazyFutureStream#boundedWaitFree(int size)
	 */
	LazyFutureStream<U> withQueueFactory(QueueFactory<U> queue);

	LazyFutureStream<U> withErrorHandler(
			Optional<Consumer<Throwable>> errorHandler);

	LazyFutureStream<U> withSubscription(Continueable sub);

	LazyFutureStream<U> withLastActive(StreamWrapper streamWrapper);
	/* 
	 * Convert this stream into an async / sync stream
	 * 
	 *	@param async true if aysnc stream
	 *	@return
	 * @see com.aol.simple.react.stream.traits.ConfigurableStream#withAsync(boolean)
	 */
	LazyFutureStream<U> withAsync(boolean async);
	
	default void cancel()	{
		FutureStream.super.cancel();
	}
	default void forEach(Consumer<? super U> c){
		LazyStream.super.forEach(c);
		
	}
	default Queue<U> toQueue(){
		return LazyToQueue.super.toQueue();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BinaryOperator)
	 */
	@Override
	default U reduce(U identity, BinaryOperator<U> accumulator) {

		return LazyStream.super.reduce(identity, accumulator);
	}
	@Override
	default<T> T reduce(T identity, BiFunction<T,? super U,T> accumulator, BinaryOperator<T> combiner){
		return LazyStream.super.reduce(identity, accumulator,combiner);
	}
	@Override
	default Optional<U> reduce(BinaryOperator<U> accumulator){
		return LazyStream.super.reduce(accumulator);
	}
	@Override
	default <R> R collect(Supplier<R> supplier, BiConsumer<R,? super U> accumulator, BiConsumer<R,R> combiner){
		return LazyStream.super.collect(supplier, accumulator, combiner);
	}
	/* 
	 * Execute subsequent stages on the completing thread (until async called)
	 * 10X faster than async execution.
	 * Use async for blocking IO or distributing work across threads or cores.
	 * Switch to sync for non-blocking tasks when desired thread utlisation reached
	 * 
	 *	@return Version of FutureStream that will use sync CompletableFuture methods
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#sync()
	 */
	default LazyFutureStream<U> sync(){
		return (LazyFutureStream<U>)FutureStream.super.sync();
	}
	/* 
	 * Execute subsequent stages by submission to an Executor for async execution
	 * 10X slower than sync execution.
	 * Use async for blocking IO or distributing work across threads or cores.
	 * Switch to sync for non-blocking tasks when desired thread utlisation reached
	 *
	 * 
	 *	@return Version of FutureStream that will use async CompletableFuture methods
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#async()
	 */
	default LazyFutureStream<U> async(){
		return (LazyFutureStream<U>)FutureStream.super.async();
	}
	
	/**
	 * This is the default setting, internal queues are backed by a ConcurrentLinkedQueue
	 * This operator will return the next stage to using this Queue type if it has been changed
	 * 
	 * @return LazyFutureStream backed by a ConcurrentLinkedQueue
	 */ 
	default LazyFutureStream<U> unboundedWaitFree(){
		return this.withQueueFactory(QueueFactories.unboundedNonBlockingQueue());
	}
	/**
	 * Use an Agrona ManyToOneConcurrentArrayQueue for the next operations (wait-free, mechanical sympathy).
	 * Note Queued data will be somewhat limited by configured concurrency level, but that flatMap operations
	 * can increase the amount of data to be buffered significantly.
	 * 
	 * @param size Buffer size
	 * @return LazyFutureStream backed by an Agrona ManyToOneConcurrentArrayQueue
	 */
	default LazyFutureStream<U> boundedWaitFree(int size){
		return this.withQueueFactory(QueueFactories.boundedNonBlockingQueue(size));
	}
	/**
	 * Configure the max active concurrent tasks. The last set value wins, this can't be set per stage.
	 * 
	 * <pre>
	 *    {@code
	 *    	List<String> data = new LazyReact().react(urlFile)
	 *    										.maxActive(100)
	 *    										.flatMap(this::loadUrls)
	 *    										.map(this::callUrls)
	 *    										.block();
	 *    }
	 * </pre>
	 * 
	 * @param concurrentTasks Maximum number of active task chains
	 * @return LazyFutureStream with new limits set
	 */
	default LazyFutureStream<U> maxActive(int concurrentTasks){
		return this.withWaitStrategy(new LimitingMonitor(
					new MaxActive(concurrentTasks, concurrentTasks)));
	}
	default <R> LazyFutureStream<R> thenSync(final Function<U, R> fn){
		return (LazyFutureStream<R>)FutureStream.super.thenSync(fn);
	}

	default void closeAll(){
		getSubscription().closeAll();
	}
	  /**
     * Returns an {@link Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty.  If the stream has
     * no encounter order, then any element may be returned.
     *
     * <p>This is a <a href="package-summary.html#StreamOps">short-circuiting
     * terminal operation</a>.
     *
     * @return an {@code Optional} describing the first element of this stream,
     * or an empty {@code Optional} if the stream is empty
     * @throws NullPointerException if the element selected is null
     */
    default Optional<U> findFirst(){
    	List<U> results = this.run(Collectors.toList());
    	if(results.size()==0)
    		return Optional.empty();
    	return Optional.of(results.get(0));
    }
	
	/**
	 * Convert between an Lazy and Eager future stream,
	 * can be used to take advantages of each approach during a single Stream
	 * 
	 * @return An EagerFutureStream from this LazyFutureStream, will use the same executors
	 */
	default EagerFutureStream<U> convertToEagerStream(){
		return new EagerReact(getTaskExecutor()).withRetrier(getRetrier()).fromStream((Stream)getLastActive().stream());
	}
	
	
	default <R> LazyFutureStream<R> map(Function<? super U, ? extends R> mapper) {
		return (LazyFutureStream<R>)FutureStream.super.map(mapper);
	}
	/**
	 * Zip this Stream with an index, but Zip based on the underlying tasks, not completed results.
	 * 
	 * e.g.
	 * two functions that return method name, but take varying lengths of time.
	 * <code>
	 * 
	 * LazyFutureStream.react(()-gt;takesALotOfTime(),()-gt;veryQuick()).zipWithIndex();
	 * 
	 *  [["takesALotOfTime",0],["veryQuick",1]]
	 * 
	 *  </code>
	 *  
	 *  Where as with standard zipWithIndex you would get a new Stream ordered by completion
	 *  <code>
	 *  
	 *  [["veryQuick",0],["takesALotOfTime",1]]
	 *  
	 *  </code>
	 *  Care should be taken not to use this method with infinite streams!
	 * 
	 * @return Zipped Sequence 
	 */
	default LazyFutureStream<Tuple2<U,Long>> zipFuturesWithIndex() {

		Seq seq = Seq.seq(getLastActive().stream().iterator()).zipWithIndex();
		Seq<Tuple2<CompletableFuture<U>,Long>> withType = (Seq<Tuple2<CompletableFuture<U>,Long>>)seq;
		Stream futureStream =  withType.map(t ->t.v1.thenApplyAsync(v -> Tuple.tuple(t.v1.join(),t.v2)));
		FutureStream noType = fromStreamOfFutures(futureStream);
		
		return (LazyFutureStream<Tuple2<U,Long>>)noType;
		
	}
	/**
	 *  Duplicate a Stream into two equivalent Sequences
	 *  LazyFutureStream.of(1, 2, 3).duplicate()
	 * 
	 * results in
	 * 
	 * tuple((1,2,3),(1,2,3))
	 * 
	 * Care should be taken not to use this method with infinite streams!
	 * 
	 * 
	 * @see #duplicate()
	 * 
	 * @return Two equivalent Streams
	 */
	default Tuple2<Seq<U>, Seq<U>> duplicateFuturesSeq() {
		
		Stream stream = getLastActive().stream();
		Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> duplicated = Seq
				.seq((Stream<CompletableFuture<U>>) stream).duplicate();
		return new Tuple2(fromStreamOfFutures(duplicated.v1),
				fromStreamOfFutures(duplicated.v2));
	}
	/**
	 * Duplicate a Stream into two equivalent LazyFutureStreams
	 * 
	 * LazyFutureStream.of(1, 2, 3).duplicate()
	 * 
	 * results in
	 * 
	 * tuple((1,2,3),(1,2,3))
	 * 
	 * Care should be taken not to use this method with infinite streams!
	 * 
	 * @return Two equivalent Streams
	 * 
	 * @see #duplicate()
	 */
	default Tuple2<LazyFutureStream<U>, LazyFutureStream<U>> duplicateFutures() {
		Tuple2 dup = duplicateFuturesSeq();
		return (Tuple2<LazyFutureStream<U>,LazyFutureStream<U>>) dup;
	}
	/**
	 * Zip two Streams, zipping against the underlying futures of this stream
	 * 
	 * @param other
	 * @return
	 */
	default <R> LazyFutureStream<Tuple2<U,R>> zipFutures(Stream<R> other) {
		return (LazyFutureStream<Tuple2<U,R>>)FutureStream.super.zipFutures(other);

	}
	/**
	 * Zip two Streams, zipping against the underlying futures of both Streams
	 * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
	 * will be populated asyncrhonously
	 * 
	 * @param other  Another FutureStream to zip Futures with
	 * @return New Sequence of CompletableFutures
	 */

	default <R> LazyFutureStream<Tuple2<U,R>> zipFutures(FutureStream<R> other) {
		return (LazyFutureStream<Tuple2<U,R>>)FutureStream.super.zipFutures(other);

	}
	
	
	/**
	 * @return a Stream that batches all completed elements from this stream
	 *         since last read attempt into a collection
	 */
	default LazyFutureStream<Collection<U>> chunkSinceLastRead() {
		return (LazyFutureStream<Collection<U>>) FutureStream.super
				.chunkSinceLastRead();
	}

	/**
	 * Break a stream into multiple Streams based of some characteristic of the
	 * elements of the Stream
	 * 
	 * e.g.
	 * <pre>
	 * <code>
	 * 
	 * LazyFutureStream.of(10,20,25,30,41,43).shard(ImmutableMap.of("even",new
	 * 															Queue(),"odd",new Queue(),element-&gt; element%2==0? "even" : "odd");
	 * 
	 * </code>
	 * </pre>
	 * results in 2 Streams "even": 10,20,30 "odd" : 25,41,43
	 * 
	 * @param shards
	 *            Map of Queue's keyed by shard identifier
	 * @param sharder
	 *            Function to split split incoming elements into shards
	 * @return Map of new sharded Streams
	 */
	default <K> Map<K, LazyFutureStream<U>> shard(Map<K, Queue<U>> shards,
			Function<U, K> sharder) {
		Map map = FutureStream.super.shard(shards, sharder);
		return (Map<K, LazyFutureStream<U>>) map;
	}

	/**
	 * Can be used to debounce (accept a single data point from a unit of time)
	 * data. This drops data. For a method that slows emissions and keeps data
	 * #see#onePer
	 * 
	 * @param time
	 *            Time from which to accept only one element
	 * @param unit
	 *            Time unit for specified time
	 * @return Next stage of stream, with only 1 element per specified time
	 *         windows
	 */
	default LazyFutureStream<U> debounce(long time, TimeUnit unit) {
		return (LazyFutureStream<U>) FutureStream.super.debounce(time, unit);
	}

	/**
	 * Return a Stream with the same values as this Stream, but with all values
	 * omitted until the provided stream starts emitting values. Provided Stream
	 * ends the stream of values from this stream.
	 * 
	 * @param s
	 *            Stream that will start the emission of values from this stream
	 * @return Next stage in the Stream but with all values skipped until the
	 *         provided Stream starts emitting
	 */
	default <T> LazyFutureStream<U> skipUntil(FutureStream<T> s) {
		return (LazyFutureStream<U>) FutureStream.super.skipUntil(s);
	}

	/**
	 * Return a Stream with the same values, but will stop emitting values once
	 * the provided Stream starts to emit values. e.g. if the provided Stream is
	 * asynchronously refreshing state from some remote store, this stream can
	 * proceed until the provided Stream succeeds in retrieving data.
	 * 
	 * @param s
	 *            Stream that will stop the emission of values from this stream
	 * @return Next stage in the Stream but will only emit values until provided
	 *         Stream starts emitting values
	 */
	default <T> LazyFutureStream<U> takeUntil(FutureStream<T> s) {
		return (LazyFutureStream<U>) FutureStream.super.takeUntil(s);
	}

	/**
	 * Allows clients to control the emission of data for the next phase of the
	 * Stream. The user specified function can delay, drop, or change elements
	 * 
	 * @param fn
	 *            Function takes a supplier, which can be used repeatedly to get
	 *            the next value from the Stream. If there are no more values, a
	 *            ClosedQueueException will be thrown. This function should
	 *            return a Supplier which returns the desired result for the
	 *            next element (or just the next element).
	 * @return Next stage in Stream
	 */
	default LazyFutureStream<U> control(Function<Supplier<U>, Supplier<U>> fn) {
		return (LazyFutureStream<U>) FutureStream.super.control(fn);
	}

	/**
	 * Batch elements into a Stream of collections with user defined function
	 * 
	 * @param fn
	 *            Function takes a supplier, which can be used repeatedly to get
	 *            the next value from the Stream. If there are no more values, a
	 *            ClosedQueueException will be thrown. This function should
	 *            return a Supplier which creates a collection of the batched
	 *            values
	 * @return Stream of batched values
	 */
	default LazyFutureStream<Collection<U>> batch(
			Function<Supplier<U>, Supplier<Collection<U>>> fn) {
		return (LazyFutureStream<Collection<U>>) FutureStream.super.batch(fn);
	}

	/**
	 * 
	 * Batch the elements in this stream into Lists of specified size
	 * 
	 * @param size
	 *            Size of lists elements should be batched into
	 * @return Stream of Lists
	 */
	default LazyFutureStream<Collection<U>> batchBySize(int size) {
		return (LazyFutureStream<Collection<U>>) FutureStream.super
				.batchBySize(size);

	}

	/**
	 * Batch the elements in this stream into Collections of specified size The
	 * type of Collection is determined by the specified supplier
	 * 
	 * @param size
	 *            Size of batch
	 * @param supplier
	 *            Create the batch holding collection
	 * @return Stream of Collections
	 */
	default LazyFutureStream<Collection<U>> batchBySize(int size,
			Supplier<Collection<U>> supplier) {
		return (LazyFutureStream<Collection<U>>) FutureStream.super
				.batchBySize(size, supplier);

	}
	
	/**
	 * Introduce a random delay between events in a stream Can be used to
	 * prevent behaviour synchronizing within a system
	 * 
	 * @param jitterInNanos
	 *            Max number of nanos for jitter (random number less than this
	 *            will be selected)/
	 * @return Next stage in Stream with jitter applied
	 */
	default LazyFutureStream<U> jitter(long jitterInNanos) {
		return (LazyFutureStream<U>) FutureStream.super.jitter(jitterInNanos);
	}

	/**
	 * Apply a fixed delay before emitting elements to the next phase of the
	 * Stream. Note this doesn't neccessarily imply a fixed delay between
	 * element creation (although it may do). e.g.
	 * 
	 * LazyFutureStream.of(1,2,3,4).fixedDelay(1,TimeUnit.hours);
	 * 
	 * Will emit 1 on start, then 2 after an hour, 3 after 2 hours and so on.
	 * 
	 * However all 4 numbers will be populated in the Stream immediately.
	 * 
	 * LazyFutureStream.of(1,2,3,4).withQueueFactories(QueueFactories.
	 * boundedQueue(1)).fixedDelay(1,TimeUnit.hours);
	 * 
	 * Will populate each number in the Stream an hour apart.
	 * 
	 * @param time
	 *            amount of time between emissions
	 * @param unit
	 *            TimeUnit for emissions
	 * @return Next Stage of the Stream
	 */
	default LazyFutureStream<U> fixedDelay(long time, TimeUnit unit) {
		return (LazyFutureStream<U>) FutureStream.super.fixedDelay(time, unit);
	}

	/**
	 * Slow emissions down, emiting one element per specified time period
	 * 
	 * @param time
	 *            Frequency period of element emission
	 * @param unit
	 *            Time unit for frequency period
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	default LazyFutureStream<U> onePer(long time, TimeUnit unit) {
		return (LazyFutureStream<U>) FutureStream.super.onePer(time, unit);

	}

	/**
	 * Allows x (specified number of) emissions with a time period before
	 * stopping emmissions until specified time has elapsed since last emission
	 * 
	 * @param x
	 *            Number of allowable emissions per time period
	 * @param time
	 *            Frequency time period
	 * @param unit
	 *            Frequency time unit
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	default FutureStream<U> xPer(int x, long time, TimeUnit unit) {
		return (LazyFutureStream<U>) FutureStream.super.xPer(x, time, unit);
	}

	/**
	 * Organise elements in a Stream into a Collections based on the time period
	 * they pass through this stage
	 * 
	 * @param time
	 *            Time period during which all elements should be collected
	 * @param unit
	 *            Time unit during which all elements should be collected
	 * @return Stream of Lists
	 */
	default LazyFutureStream<Collection<U>> batchByTime(long time, TimeUnit unit) {
		return (LazyFutureStream<Collection<U>>) FutureStream.super
				.batchByTime(time, unit);
	}

	/**
	 * Organise elements in a Stream into a Collections based on the time period
	 * they pass through this stage
	 * 
	 * @param time
	 *            Time period during which all elements should be collected
	 * @param unit
	 *            Time unit during which all elements should be collected
	 * @param factory
	 *            Instantiates the collections used in the batching
	 * @return Stream of collections
	 */
	default LazyFutureStream<Collection<U>> batchByTime(long time,
			TimeUnit unit, Supplier<Collection<U>> factory) {
		return (LazyFutureStream<Collection<U>>) FutureStream.super
				.batchByTime(time, unit, factory);

	}

	/**
	 * Similar to zip and withLatest, except will always take the latest from
	 * either Stream (merged with last available from the other). By contrast
	 * zip takes new / latest values from both Streams and withLatest will
	 * always take the latest from this Stream while taking the last available
	 * value from the provided stream.
	 * 
	 * @param s
	 *            Stream to merge with
	 * @return Stream of Tuples with the latest values from either stream
	 */
	default <T> LazyFutureStream<Tuple2<U, T>> combineLatest(FutureStream<T> s) {
		return (LazyFutureStream<Tuple2<U, T>>) FutureStream.super
				.combineLatest(s);
	}

	/**
	 * 
	 * Similar to zip and combineLatest, except will always take the latest from
	 * this Stream while taking the last available value from the provided
	 * stream. By contrast zip takes new / latest values from both Streams and
	 * combineLatest takes the latest from either Stream (merged with last
	 * available from the other).
	 * 
	 * @param s
	 *            Stream to merge with
	 * @return Stream of Tuples with the latest values from this stream
	 */
	default <T> LazyFutureStream<Tuple2<U, T>> withLatest(FutureStream<T> s) {
		return (LazyFutureStream<Tuple2<U, T>>) FutureStream.super
				.withLatest(s);
	}

	/**
	 * Return first Stream out of provided Streams that starts emitted results 
	 * 
	 * @param futureStreams Streams to race
	 * @return First Stream to start emitting values
	 */
	@SafeVarargs
	static <U> LazyFutureStream<U> firstOf(LazyFutureStream<U>... futureStreams) {
		return (LazyFutureStream<U>) FutureStream.firstOf(futureStreams);
	}

	/*
	 * 
	 * React to new events with the supplied function on the supplied
	 * Executor 
	 * 
	 * @param fn Apply to incoming events
	 * 
	 * @param service Service to execute function on
	 * 
	 * @return next stage in the Stream
	 */
	default <R> LazyFutureStream<R> then(final Function<U, R> fn,
			Executor service) {
		return (LazyFutureStream<R>) FutureStream.super.then(fn, service);
	}

	/**
	 * Override return type on SimpleReactStream
	 */

	/*
	 * Non-blocking asyncrhonous application of the supplied function.
	 * Equivalent to map from Streams / Seq apis.
	 * 
	 * @param fn Function to be applied asynchronously
	 * 
	 * @return Next stage in stream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#then(java.util.function
	 * .Function)
	 */
	default <R> LazyFutureStream<R> then(final Function<U, R> fn) {
		return (LazyFutureStream) FutureStream.super.then(fn);
	}

	/*
	 * Merge two SimpleReact Streams
	 * 
	 * @param s Stream to merge
	 * 
	 * @return Next stage in stream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#merge(com.aol.simple.
	 * react.stream.traits.SimpleReactStream)
	 */
	@Override
	default LazyFutureStream<U> merge(SimpleReactStream<U> s) {
		return (LazyFutureStream) FutureStream.super.merge(s);
	}

	/*
	 * Define failure handling for this stage in a stream. Recovery function
	 * will be called after an exception Will be passed a
	 * SimpleReactFailedStageException which contains both the cause, and the
	 * input value.
	 * 
	 * @param fn Recovery function
	 * 
	 * @return Next stage in stream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#onFail(java.util.function
	 * .Function)
	 */
	@Override
	default LazyFutureStream<U> onFail(
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (LazyFutureStream) FutureStream.super.onFail(fn);
	}

	/*
	 * Handle failure for a particular class of exceptions only
	 * 
	 * @param exceptionClass Class of exceptions to handle
	 * 
	 * @param fn recovery function
	 * 
	 * @return recovered value
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#onFail(java.lang.Class,
	 * java.util.function.Function)
	 */
	@Override
	default LazyFutureStream<U> onFail(
			Class<? extends Throwable> exceptionClass,
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (LazyFutureStream) FutureStream.super.onFail(exceptionClass, fn);
	}

	/*
	 * Capture non-recoverable exception
	 * 
	 * @param errorHandler Consumer that captures the exception
	 * 
	 * @return Next stage in stream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#capture(java.util.function
	 * .Consumer)
	 */
	@Override
	default LazyFutureStream<U> capture(
			final Consumer<? extends Throwable> errorHandler) {
		return (LazyFutureStream) FutureStream.super.capture(errorHandler);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#allOf(java.util.function
	 * .Function)
	 */
	@Override
	default <T, R> LazyFutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (LazyFutureStream) FutureStream.super.allOf(fn);
	}

	default <R> LazyFutureStream<R> anyOf(Function<U, R> fn) {

		return (LazyFutureStream) FutureStream.super.anyOf(fn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function
	 * .Consumer)
	 */
	@Override
	default LazyFutureStream<U> peek(final Consumer<? super U> consumer) {
		return (LazyFutureStream) FutureStream.super.peek(consumer);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function
	 * .Predicate)
	 */
	default LazyFutureStream<U> filter(final Predicate<? super U> p) {
		return (LazyFutureStream) FutureStream.super.filter(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#flatMap(java.util.function
	 * .Function)
	 */
	@Override
	default <R> LazyFutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		return (LazyFutureStream) FutureStream.super.flatMap(flatFn);
	}

	/**
	 * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
	 * If in async mode this operation is performed asyncrhonously
	 * If in sync mode this operation is performed synchronously
	 * 
	 * <pre>
	 * {@code 
	 * assertThat( new LazyReact()
										.of(1,2,3)
										.flatMapCompletableFuture(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
	 * 
	 * 
	 * @param flatFn flatMap function
	 * @return Flatten Stream with flatFn applied
	 */
	default <R> LazyFutureStream<R> flatMapCompletableFuture(
			Function<U, CompletableFuture<R>> flatFn) {
		return (LazyFutureStream) FutureStream.super.flatMapCompletableFuture(flatFn);
	}
	/**
	 * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
	 * This operation is performed synchronously
	 * 
	 * <pre>
	 * {@code 
	 * assertThat( new LazyReact()
										.of(1,2,3)
										.flatMapCompletableFutureSync(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
	 * 
	 * 
	 * @param flatFn flatMap function
	 * @return Flatten Stream with flatFn applied
	 */
	default <R> LazyFutureStream<R> flatMapCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn) {
		
		return (LazyFutureStream) FutureStream.super.flatMapCompletableFutureSync(flatFn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#retry(java.util.function
	 * .Function)
	 */
	@Override
	default <R> LazyFutureStream<R> retry(Function<U, R> fn) {

		return (LazyFutureStream) FutureStream.super.retry(fn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#allOf(java.util.stream.Collector
	 * , java.util.function.Function)
	 */
	@Override
	default <T, R> LazyFutureStream<R> allOf(Collector collector,
			Function<T, R> fn) {

		return (LazyFutureStream) FutureStream.super.allOf(collector, fn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#fromStream(java.util.stream
	 * .Stream)
	 */
	@Override
	default <R> LazyFutureStream<R> fromStream(Stream<R> stream) {

		return (LazyFutureStream) FutureStream.super.fromStream(stream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#fromStreamCompletableFuture
	 * (java.util.stream.Stream)
	 */
	@Override
	default <R> LazyFutureStream<R> fromStreamOfFutures(
			Stream<CompletableFuture<R>> stream) {

		return (LazyFutureStream) FutureStream.super
				.fromStreamOfFutures(stream);
	}

	/**
	 * Concatenate two streams.
	 * 
	 * 
	 * // (1, 2, 3, 4, 5, 6) LazyFutureStream.of(1, 2,
	 * 3).concat(LazyFutureStream.of(4, 5, 6))
	 * 
	 *
	 * @see #concat(Stream[])
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	default LazyFutureStream<U> concat(Stream<U> other) {

		SimpleReactStream stream = other instanceof SimpleReactStream ? (SimpleReactStream) other
				: SimpleReactStream.sequentialCommonBuilder()
						.of(other);
		return (LazyFutureStream) merge(stream);
	}

	/*
	 * Cast all elements in this stream to specified type. May throw {@link
	 * ClassCastException}.
	 * 
	 * LazyFutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 * will throw a ClassCastException
	 * 
	 * @param type Type to cast to
	 * 
	 * @return LazyFutureStream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#cast(java.lang.Class)
	 */
	@Override
	default <U> LazyFutureStream<U> cast(Class<U> type) {
		return (LazyFutureStream<U>) FutureStream.super.cast(type);
	}

	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * 
	 * LazyFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
	 * 
	 * gives a Stream of (1,2,3)
	 * 
	 * LazyFutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
	 * 
	 * gives a Stream of ("a","b")
	 * 
	 * @see com.aol.simple.react.stream.traits.FutureStream#ofType(java.lang.Class)
	 */
	@Override
	default <U> FutureStream<U> ofType(Class<U> type) {
		return (LazyFutureStream<U>) FutureStream.super.ofType(type);
	}

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * <code>
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) 
	 * 
	 * LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * 
	 * </code>
	 *
	 * @see #intersperse(Stream, Object)
	 */
	@Override
	default LazyFutureStream<U> intersperse(U value) {
		return (LazyFutureStream<U>) FutureStream.super.intersperse(value);
	}

	/*
	 * 
	 * LazyFutureStream.of(1,2,3,4).limit(2)
	 * 
	 * Will result in a Stream of (1,2). Only the first two elements are used.
	 * 
	 * @param maxSize number of elements to take
	 * 
	 * @return Limited LazyFutureStream
	 * 
	 * @see org.jooq.lambda.Seq#limit(long)
	 */
	@Override
	default LazyFutureStream<U> limit(long maxSize) {

		Continueable sub = this.getSubscription();
		sub.registerLimit(maxSize);
		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withStream(lastActive.stream()
				.limit(maxSize));
		return this.withLastActive(limited);

	}

	/*
	 * LazyFutureStream.of(1,2,3,4).skip(2)
	 * 
	 * Will result in a stream of (3,4). The first two elements are skipped.
	 * 
	 * @param n Number of elements to skip
	 * 
	 * @return LazyFutureStream missing skipped elements
	 * 
	 * @see org.jooq.lambda.Seq#skip(long)
	 */
	@Override
	default LazyFutureStream<U> skip(long n) {
		Continueable sub = this.getSubscription();
		sub.registerSkip(n);
		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withStream(lastActive.stream().skip(
				n));
		return this.withLastActive(limited);

	}

	/**
	 * Construct an Lazy SimpleReact Stream from specified array
	 * 
	 * @param array
	 *            Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> LazyFutureStream<U> parallel(U... array) {
		return new LazyReact().of(Arrays.asList(array));
	}

	/*
	 * @return distinct elements in this Stream (must be a finite stream!)
	 * 
	 * @see org.jooq.lambda.Seq#distinct()
	 */
	@Override
	default Seq<U> distinct() {

		return toQueue().stream(getSubscription()).distinct();
	}

	/**
	 * Duplicate a Streams into two equivalent Streams.
	 * 
	 * <code> 
	 * 
	 * // tuple((1, 2, 3), (1, 2, 3)) 
	 * 
	 * LazyFutureStream.of(1, 2, 3).duplicate()
	 * </code>
	 *
	 * @see #duplicate(Stream)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> duplicate() {
		Tuple2<Seq<U>, Seq<U>> duplicated = FutureStream.super.duplicate();
		return new Tuple2(duplicated.v1, duplicated.v2);
	}

	/**
	 * Partition a stream into two given a predicate.
	 * 
	 * <code>
	 * 
	 * // tuple((1, 3, 5), (2, 4, 6)) 
	 * 
	 * LazyFutureStream.of(1, 2, 3, 4, 5,6).partition(i -&gt; i % 2 != 0)
	 * 
	 * </code>
	 * 
	 * @see #partition(Stream, Predicate)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> partition(Predicate<? super U> predicate) {
		Tuple2<Seq<U>, Seq<U>> partitioned = FutureStream.super
				.partition(predicate);
		return new Tuple2(partitioned.v1, partitioned.v2);
	}

	@Override
	default LazyFutureStream<U> slice(long from, long to) {

		return fromStream(FutureStream.super.slice(from, to));
	}

	/**
	 * Zip a Stream with a corresponding Stream of indexes.
	 * 
	 * <code>
	 * 
	 * // (tuple("a", 0), tuple("b", 1), tuple("c", 2)) 
	 * 
	 * LazyFutureStream.of("a","b", "c").zipWithIndex()
	 * 
	 *</code>
	 *
	 * @see #zipWithIndex(Stream)
	 * 
	 *      
	 */
	default LazyFutureStream<Tuple2<U, Long>> zipWithIndex() {
		return (LazyFutureStream)fromStream(FutureStream.super.zipWithIndex());
	}

	/**
	 * Zip two streams into one.
	 * <p>
	 * <code>
	 * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
	 * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"))
	 * </code>
	 *
	 * @see #zip(Stream, Stream)
	 */
	default <T> LazyFutureStream<Tuple2<U, T>> zip(Seq<T> other) {
		return fromStream(zip(this, other));
	}

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting
	 * values.
	 * <p>
	 * <code>
	 * // ("1:a", "2:b", "3:c")
	 * LazyFutureStream.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -&gt; i + ":" + s)
	 * </code>
	 *
	 * @see #zip(Seq, BiFunction)
	 */
	default <T, R> LazyFutureStream<R> zip(Seq<T> other,
			BiFunction<U, T, R> zipper) {
		return fromStream(zip(this, other, zipper));
	}

	/**
	 * Scan a stream to the left.
	 * 
	 * 
	 * // ("", "a", "ab", "abc") LazyFutureStream.of("a", "b", "c").scanLeft("",
	 * (u, t) &gt; u + t)
	 * 
	 */
	@Override
	default <T> LazyFutureStream<T> scanLeft(T seed,
			BiFunction<T, ? super U, T> function) {
		return fromStream(FutureStream.super.scanLeft(seed, function));
	}

	/**
	 * Scan a stream to the right. - careful with infinite streams!
	 * 
	 * 
	 * // ("", "c", "cb", "cba") LazyFutureStream.of("a", "b",
	 * "c").scanRight("", (t, u) &gt; u + t)
	 * 
	 */
	@Override
	default <R> LazyFutureStream<R> scanRight(R seed,
			BiFunction<? super U, R, R> function) {
		return (LazyFutureStream)FutureStream.super.scanRight(seed, function);
	}

	/**
	 * Reverse a stream. - careful with infinite streams!
	 * 
	 * 
	 * // (3, 2, 1) LazyFutureStream.of(1, 2, 3).reverse()
	 * 
	 */
	@Override
	default LazyFutureStream<U> reverse() {
		return (LazyFutureStream)FutureStream.super.reverse();
	}

	/**
	 * Shuffle a stream
	 * 
	 * 
	 * // e.g. (2, 3, 1) LazyFutureStream.of(1, 2, 3).shuffle()
	 * 
	 */
	@Override
	default LazyFutureStream<U> shuffle() {
		return fromStream(FutureStream.super.shuffle());
	}

	/**
	 * Shuffle a stream using specified source of randomness
	 * 
	 * 
	 * // e.g. (2, 3, 1) LazyFutureStream.of(1, 2, 3).shuffle(new Random())
	 * 
	 */
	@Override
	default LazyFutureStream<U> shuffle(Random random) {
		return fromStream(FutureStream.super.shuffle(random));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to true.
	 * 
	 * 
	 * // (3, 4, 5) LazyFutureStream.of(1, 2, 3, 4, 5).skipWhile(i &gt; i &lt;
	 * 3)
	 * 
	 *
	 * @see #skipWhile(Stream, Predicate)
	 */
	@Override
	default LazyFutureStream<U> skipWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipWhile(predicate));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to false.
	 * 
	 * 
	 * // (3, 4, 5) LazyFutureStream.of(1, 2, 3, 4, 5).skipUntil(i &gt; i == 3)
	 * 
	 *
	 * @see #skipUntil(Stream, Predicate)
	 */
	@Override
	default LazyFutureStream<U> skipUntil(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipUntil(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to true.
	 * 
	 * 
	 * // (1, 2) LazyFutureStream.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
	 * 
	 *
	 * @see #limitWhile(Stream, Predicate)
	 */
	@Override
	default LazyFutureStream<U> limitWhile(Predicate<? super U> predicate) {
		return fromStream(LazyFutureStream.limitWhile(this, predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to false.
	 * 
	 * 
	 * // (1, 2) LazyFutureStream.of(1, 2, 3, 4, 5).limitUntil(i &gt; i == 3)
	 * 
	 *
	 * @see #limitUntil(Stream, Predicate)
	 */
	@Override
	default LazyFutureStream<U> limitUntil(Predicate<? super U> predicate) {
		return fromStream(limitUntil(this, predicate));
	}

	/**
	 * Construct a SimpleReact Stage from a supplied array
	 * 
	 * @param array
	 *            Array of value to form the reactive stream / sequence
	 * @return SimpleReact Stage
	 */
	public static <U> LazyFutureStream<U> parallelOf(U... array) {
		return new LazyReact().of(Arrays.asList(array));
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
	 *  Create a parallel asynchronous stream
	 * @see Stream#of(Object)
	 */
	static <T> LazyFutureStream<T> react(Supplier<T> value) {
		return  new LazyReact().react(value);
	}

	/**
	 * Create a parallel asynchronous stream
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> react(Supplier<T>... values) {
		return  new LazyReact().react(values);
	}
	/**
	 * @see Stream#of(Object)
	 */
	static <T> LazyFutureStream<T> of(T value) {
		return of((Stream) Seq.of(value));
	}

	/**
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> of(T... values) {
		return of((Stream) Seq.of(values));
	}

	/**
	 * @see Stream#empty()
	 */
	static <T> LazyFutureStream<T> empty() {
		return of((Stream) Seq.empty());
	}

	/**
	 * @see Stream#iterate(Object, UnaryOperator)
	 */
	static <T> LazyFutureStream<T> iterate(final T seed,
			final UnaryOperator<T> f) {
		return of((Stream) Seq.iterate(seed, f));
	}

	/**
	 * @see Stream#generate(Supplier)
	 */
	static LazyFutureStream<Void> generate() {
		return generate(() -> null);
	}

	/**
	 * @see Stream#generate(Supplier)
	 */
	static <T> LazyFutureStream<T> generate(T value) {
		return generate(() -> value);
	}

	/**
	 * @see Stream#generate(Supplier)
	 */
	static <T> LazyFutureStream<T> generate(Supplier<T> s) {
		return of(Stream.generate(s));
	}

	/**
	 * Wrap a Stream into a FutureStream.
	 */
	static <T> LazyFutureStream<T> of(Stream<T> stream) {
		if (stream instanceof LazyFutureStream)
			return (LazyFutureStream<T>) stream;
		if (stream instanceof FutureStream)
			stream = ((FutureStream) stream).toQueue().stream(
					((FutureStream) stream).getSubscription());
		LazyReact react =new LazyReact(ThreadPools.getSequential(), RetryBuilder.getDefaultInstance()
						.withScheduler(ThreadPools.getSequentialRetry()),false, new MaxActive(1,1));
		return new LazyFutureStreamImpl<T>(react,
				stream.map(CompletableFuture::completedFuture));
	}

	/**
	 * Wrap an Iterable into a FutureStream.
	 */
	static <T> LazyFutureStream<T> ofIterable(Iterable<T> iterable) {
		return of(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream.
	 */
	static <T> LazyFutureStream<T> of(Iterator<T> iterator) {
		return of(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}

	/**
	 * Zip two streams into one.
	 * <p>
	 * <code>
	 * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
	 * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"))
	 * </code>
	 */
	static <T1, T2> Seq<Tuple2<T1, T2>> zip(Stream<T1> left, Stream<T2> right) {
		return zip(left, right, Tuple::tuple);
	}

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting
	 * values.
	 * <p>
	 * <code>
	 * // ("1:a", "2:b", "3:c")
	 * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -&gt; i + ":" + s)
	 * </code>
	 */
	static <T1, T2, R> Seq<R> zip(Stream<T1> left, Stream<T2> right,
			BiFunction<T1, T2, R> zipper) {
		final Iterator<T1> it1 = left.iterator();
		final Iterator<T2> it2 = right.iterator();

		class Zip implements Iterator<R> {
			@Override
			public boolean hasNext() {
				if (!it1.hasNext()) {
					close(it2);
				}
				if (!it2.hasNext()) {
					close(it1);
				}
				return it1.hasNext() && it2.hasNext();
			}

			@Override
			public R next() {
				return zipper.apply(it1.next(), it2.next());
			}
		}

		return Seq.seq(new Zip());
	}

	static void close(Iterator it) {

		if (it instanceof CloseableIterator) {
			((CloseableIterator) it).close();
		}
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to <code>true</code>.
	 * <p>
	 * <code>
	 * // (1, 2)
	 * Seq.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
	 * </code>
	 */
	static <T> Seq<T> limitWhile(Stream<T> stream,
			Predicate<? super T> predicate) {
		return limitUntil(stream, predicate.negate());
	}

	public final static Object NULL = new Object();

	/**
	 * Returns a stream ed to all elements for which a predicate evaluates to
	 * <code>true</code>.
	 * <p>
	 * <code>
	 * // (1, 2)
	 * Seq.of(1, 2, 3, 4, 5).limitUntil(i -&gt; i == 3)
	 * </code>
	 */
	@SuppressWarnings("unchecked")
	static <T> Seq<T> limitUntil(Stream<T> stream,
			Predicate<? super T> predicate) {
		final Iterator<T> it = stream.iterator();

		class LimitUntil implements Iterator<T> {
			T next = (T) NULL;
			boolean test = false;

			void test() {
				if (!test && next == NULL && it.hasNext()) {
					next = it.next();

					if (test = predicate.test(next)) {
						next = (T) NULL;
						close(it); // need to close any open queues
					}
				}
			}

			@Override
			public boolean hasNext() {
				test();
				return next != NULL;
			}

			@Override
			public T next() {
				if (next == NULL)
					throw new NoSuchElementException();

				try {
					return next;
				} finally {
					next = (T) NULL;
				}
			}
		}

		return Seq.seq(new LimitUntil());
	}
	
	

	/* 
	 *	@return Convert to standard JDK 8 Stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#stream()
	 */ 
	@Override
	default Stream<U> stream() {
		return FutureStream.super.stream();
	}

	/* 
	 *	@return New version of this stream converted to execute asynchronously and in parallel
	 * @see com.aol.simple.react.stream.traits.FutureStream#parallel()
	 */
	default LazyFutureStream<U> parallel(){
		return this.withAsync(true).withTaskExecutor(parallelBuilder().getExecutor());
	}
	/* 
	 *	@return New version of this stream  converted to execute synchronously and sequentially
	 * @see com.aol.simple.react.stream.traits.FutureStream#sequential()
	 */
	default LazyFutureStream<U> sequential(){
		return this.withAsync(false).withTaskExecutor(sequentialBuilder().getExecutor());
	}


	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#unordered()
	 */
	@Override
	default LazyFutureStream<U> unordered() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
	 */
	@Override
	default LazyFutureStream<U> onClose(Runnable closeHandler) {

		return (LazyFutureStream)FutureStream.super.onClose(closeHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted()
	 */
	@Override
	default LazyFutureStream<U> sorted() {
		return (LazyFutureStream<U>)fromStream(FutureStream.super.sorted());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	default LazyFutureStream<U> sorted(Comparator<? super U> comparator) {
		return (LazyFutureStream<U>)fromStream(FutureStream.super.sorted(comparator));
	}

	/**
	 * Give a function access to the current stage of a SimpleReact Stream
	 * 
	 * @param consumer
	 *            Consumer that will recieve current stage
	 * @return Self (current stage)
	 */
	default LazyFutureStream<U> self(Consumer<FutureStream<U>> consumer) {
		return (com.aol.simple.react.stream.traits.LazyFutureStream<U>)FutureStream.super.self(consumer);
	}

	LazyFutureStream<U> withParallelReduction(
			ParallelReductionConfig parallelReductionConfig);


}
