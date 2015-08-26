package com.aol.simple.react.stream.traits;

import static com.aol.simple.react.stream.traits.NullValue.NULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.ArrayList;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.Queue.QueueTimeoutException;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.reactivestreams.FutureStreamAsyncPublisher;
import com.aol.simple.react.reactivestreams.FutureStreamSynchronousPublisher;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyFutureStreamImpl;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.traits.future.operators.ToLazyCollection;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */

public interface LazyFutureStream<U> extends  LazySimpleReactStream<U>,LazyStream<U>,FutureStream<U>, LazyToQueue<U>,
										ConfigurableStream<U,FastFuture<U>>,
										FutureStreamAsyncPublisher<U>,
										FutureStreamSynchronousPublisher<U> {

	Continueable getSubscription();
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

	LazyFutureStream<U> withWaitStrategy(Consumer<FastFuture> c);

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

	LazyFutureStream<U> withLastActive(LazyStreamWrapper streamWrapper);
	/* 
	 * Convert this stream into an async / sync stream
	 * 
	 *	@param async true if aysnc stream
	 *	@return
	 * @see com.aol.simple.react.stream.traits.ConfigurableStream#withAsync(boolean)
	 */
	LazyFutureStream<U> withAsync(boolean async);
	
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
		return this.withAsync(false);
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
		return this.withAsync(true);
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
		return (LazyFutureStream<R>)LazySimpleReactStream.super.thenSync(fn);
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
	
	/* 
	 * Apply a function to all items in the stream.
	 * <pre>
	 * {@code
	 *  LazyFutureStream.sequentialBuilder().react(()->1,()->2,()->3)
		 									 .map(it->it+100) //add 100
		 									 .toList();
	 * }
	 * //results in [100,200,300]
	 * </pre>
	 *	@param mapper Function to be applied to all items in the Stream
	 *	@return
	 * @see com.aol.simple.react.stream.traits.FutureStream#map(java.util.function.Function)
	 */
	default <R> LazyFutureStream<R> map(Function<? super U, ? extends R> mapper) {
		return (LazyFutureStream<R>)LazySimpleReactStream.super.then((Function)mapper);
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
		return fromStream(seq);
		
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
		
		Tuple2<Seq<FastFuture<U>>, Seq<FastFuture<U>>> duplicated = Seq
				.seq((Stream<FastFuture<U>>) stream).duplicate();
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
		if(other instanceof FutureStream)
			return zipFutures((FutureStream)other);
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other));
	
		return fromStream(seq);

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
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other.getLastActive().stream()));
		return fromStream(seq);

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
		return (LazyFutureStream<U>)FutureStream.super.control(fn);
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
	default <C extends Collection<U>> LazyFutureStream<C> batch(
			Function<Supplier<U>, Supplier<C>> fn) {
		return (LazyFutureStream<C>) FutureStream.super.batch(fn);
	}

	/**
	 * 
	 * Batch the elements in this stream into Lists of specified size
	 * 
	 * @param size
	 *            Size of lists elements should be batched into
	 * @return Stream of Lists
	 */
	default  LazyFutureStream<List<U>> batchBySize(int size) {
		return (LazyFutureStream<List<U>>) FutureStream.super
				.batchBySize(size);

	}
	/* 
	 * Batch the elements in the Stream by a combination of Size and Time
	 * If batch exceeds max size it will be split
	 * If batch exceeds max time it will be split
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * <pre>
	 * {@code
	 * assertThat(react(()->1,()->2,()->3,()->4,()->5,()->{sleep(100);return 6;})
						.batchBySizeAndTime(30,60,TimeUnit.MILLISECONDS)
						.toList()
						.get(0)
						,not(hasItem(6)));
		}
	 * </pre>
	 * 
	 * <pre>
	 * {@code
	 * 	
		assertThat(of(1,2,3,4,5,6).batchBySizeAndTime(3,10,TimeUnit.SECONDS).toList().get(0).size(),is(3));

	 * }</pre>
	 * 
	 *	@param size Max batch size
	 *	@param time Max time length
	 *	@param unit time unit
	 *	@return batched stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit)
	 */
	default LazyFutureStream<List<U>> batchBySizeAndTime(int size,long time, TimeUnit unit) {
		return (LazyFutureStream<List<U>>) FutureStream.super
				.batchBySizeAndTime(size,time,unit);

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
	default <C extends Collection<U>> LazyFutureStream<C> batchBySize(int size,
			Supplier<C> supplier) {
		return (LazyFutureStream<C>) FutureStream.super
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
	 * <pre>
	 * {@code 
	 * 	LazyFutureStream.react(()->load1(),()->load2(),()->load3(),()->load4(),()->load5(),()->load6())
	 * 					.batchByTime(15000,TimeUnit.MICROSECONDS);
	 * 
	 * }
	 * 
	 * </pre>
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
		return (LazyFutureStream<U>) FutureStreamFunctions.firstOf(futureStreams);
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
		return (LazyFutureStream<R>) LazySimpleReactStream.super.then(fn, service);
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

	/**
	 * Copy this Stream the specified number of times
	 * 
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1,2,3,4,5,6)
				.map(i->i+2)
				.copy(5)
				.forEach(s -> System.out.println(s.toList()));
	 * 
	 * }</pre>
	 * 
	 * @param times to copy this Stream
	 * @return List with specified number of copies
	 */
	default List<LazyFutureStream<U>> copy(final int times){
		return (List)LazySimpleReactStream.super.copySimpleReactStream(times);
		
	}
	
	/*
	 * Merge two simple-react Streams, by merging the Stream of underlying
	 * futures - not suitable for merging infinite Streams unless all supplied
	 * Streams are LazyFutureStreams - use  LazyFutureStream#switchOnNext for infinite Streams
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	LazyFutureStream.of(1,2,3)
	 * 											 .merge(LazyFutureStream.of(100,200,300))
												  .map(it ->it+"!!")
												  .toList();

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	 * 
	 * }
	 * </pre>
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
	default LazyFutureStream<U> merge(SimpleReactStream<U>... streams) {
		return (LazyFutureStream<U>) (Stream.of(streams).allMatch( stream -> stream instanceof LazyFutureStream) ?
							switchOnNextValue(Seq.of(streams).cast(LazyFutureStream.class) ) :LazySimpleReactStream.super.merge(streams));
	}
	/**
	 * Merges this stream and the supplied Streams into a single Stream where the next value
	 * is the next returned across any of the involved Streams. Suitable for merging infinite streams
	 * 
	 * <pre>
	 * {@code
	 * 	LazyFutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
	 * 	LazyFutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
	 * 
	 *  LazyFutureStream<Integer> merged = fast.switchOnNextValue(Stream.of(slow));  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..] 
	 * }
	 * </pre>
	 * 
	 * @param streams
	 * @return
	 */
	default <R> LazyFutureStream<R> switchOnNextValue(Stream<LazyFutureStream> streams){
		Queue queue = Queue.createMergeQueue(); 
		addToQueue(queue);
		streams.forEach(s->s.addToQueue(queue));
		
		return fromStream(queue.stream(this.getSubscription()));
	}
	/**
	 * Merges this stream and the supplied Streams into a single Stream where the next value
	 * is the next returned across any of the involved Streams. Suitable for merging infinite streams
	 * 
	 * <pre>
	 * {@code
	 * 	LazyFutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
	 * 	LazyFutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
	 * 
	 *  LazyFutureStream<Integer> merged = fast.mergeLatest(slow);  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..] 
	 * }
	 * </pre>
	 * 
	 * @param streams
	 * @return
	 */
	default <R> LazyFutureStream<R> mergeLatest(LazyFutureStream<?>... streams){
		Queue queue = Queue.createMergeQueue(); 
		addToQueue(queue);
		Seq.of(streams).forEach(s->s.addToQueue(queue));
		
		return fromStream(queue.stream(this.getSubscription()));
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
			final Function<SimpleReactFailedStageException, U> fn) {
		return (LazyFutureStream) LazySimpleReactStream.super.onFail(fn);
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
			final Function<SimpleReactFailedStageException, U> fn) {
		return (LazyFutureStream) LazySimpleReactStream.super.onFail(exceptionClass, fn);
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
		return (LazyFutureStream) LazySimpleReactStream.super.capture(errorHandler);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#allOf(java.util.function
	 * .Function)
	 */
	@Override
	default <T, R> LazyFutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (LazyFutureStream) LazySimpleReactStream.super.allOf(fn);
	}

	default <R> LazyFutureStream<R> anyOf(Function<U, R> fn) {

		return (LazyFutureStream) LazySimpleReactStream.super.anyOf(fn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function
	 * .Consumer)
	 */
	@Override
	default LazyFutureStream<U> peek(final Consumer<? super U> consumer) {
		return (LazyFutureStream) LazySimpleReactStream.super.peek(consumer);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function
	 * .Predicate)
	 */
	default LazyFutureStream<U> filter(final Predicate<? super U> p) {
		return (LazyFutureStream) LazySimpleReactStream.super.filter(p);
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

		return (LazyFutureStream) LazySimpleReactStream.super.flatMap(flatFn);
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
		return (LazyFutureStream) LazySimpleReactStream.super.flatMapCompletableFuture(flatFn);
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
		
		return (LazyFutureStream) LazySimpleReactStream.super.flatMapCompletableFutureSync(flatFn);
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

		return (LazyFutureStream) LazySimpleReactStream.super.retry(fn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#allOf(java.util.stream.Collector
	 * , java.util.function.Function)
	 
	@Override
	default <T, R> LazyFutureStream<R> allOf(Collector collector,
			Function<T, R> fn) {

		return (LazyFutureStream) FutureStream.super.allOf(collector, fn);
	}
*/
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#fromStream(java.util.stream
	 * .Stream)
	 */
	@Override
	default <R> LazyFutureStream<R> fromStream(Stream<R> stream) {

		return (LazyFutureStream) this.withLastActive(getLastActive()
				.withNewStream(stream,this.getSimpleReact()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#fromStreamCompletableFuture
	 * (java.util.stream.Stream)
	 */
	
	default <R> LazyFutureStream<R> fromStreamOfFutures(
			Stream<FastFuture<R>> stream) {

		return (LazyFutureStream) this.withLastActive(getLastActive()
				.withNewStream(stream));
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
				: LazyFutureStream.lazyFutureStream(other);
		return (LazyFutureStream) merge(stream);
	}
	 /**
     * Concatenate two streams.
     * 
     * <pre>
     * {@code
     * // (1, 2, 3, 4)
     * LazyFutureStream.of(1, 2, 3).concat(4)
     * }
     * </pre>
     *
     * @see #concat(Stream[])
     */
    default LazyFutureStream<U> concat(U other) {
        return concat(Stream.of(other));
    }

    /**
     * Concatenate two streams.
     * 
     * <pre>
     * {@code 
     * // (1, 2, 3, 4, 5, 6)
     * LazyFutureStream.of(1, 2, 3).concat(4, 5, 6)
     * }
     * </pre>
     *
     * @see #concat(Stream[])
     */
    @SuppressWarnings({ "unchecked" })
    default LazyFutureStream<U> concat(U... other) {
        return concat(Stream.of(other));
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
	default <U> LazyFutureStream<U> ofType(Class<U> type) {
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
		LazyStreamWrapper lastActive = getLastActive();
		LazyStreamWrapper limited = lastActive.withStream(lastActive.stream()
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
		LazyStreamWrapper lastActive = getLastActive();
		LazyStreamWrapper limited = lastActive.withStream(lastActive.stream().skip(
				n));
		return this.withLastActive(limited);

	}

	
	/*
	 * @return distinct elements in this Stream (must be a finite stream!)
	 * 
	 * @see org.jooq.lambda.Seq#distinct()
	 */
	@Override
	default LazyFutureStream<U> distinct() {

		return fromStream(toQueue().stream(getSubscription()).distinct());
	}
	/**
	 * Create a sliding view over this Stream
	 * 
	 * <pre>
	 * {@code 
	 * //futureStream of [1,2,3,4,5,6]
	 *		 
	 * List<List<Integer>> list = futureStream.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	 * }
	 * </pre>
	 * @param size
	 *            Size of sliding window
	 * @return Stream with sliding view over data in this stream
	 */
	@Override
	default LazyFutureStream<List<U>> sliding(int size){
		return (LazyFutureStream)FutureStream.super.sliding(size);
	}
	/**
	 * Create a sliding view over this Stream
	 * 
	 * <pre>
	 * {@code 
	 * //futureStream of [1,2,3,4,5,6,7,8]
	 *		 
	 * List<List<Integer>> list = futureStream.sliding(3,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	 * }
	 * </pre>
	 * @param size
	 *            Size of sliding window
	 * @return Stream with sliding view over data in this stream
	 */
	@Override
	default LazyFutureStream<List<U>> sliding(int size, int increment){
		return (LazyFutureStream)FutureStream.super.sliding(size,increment);
	}
	/**
	 * Duplicate a LazyFutureStream into two equivalent Streams. 
	 * Two LazyFutureStreams are 
	 * returned but Seq interface specifies return type is Seq. See duplicateFutureStream to
	 * see an alternative which returns LazyFutureStream
	 * 
	 * <pre>
	 * {@code 
	 * 
	 * // tuple((1, 2, 3), (1, 2, 3)) 
	 * 
	 * LazyFutureStream.of(1, 2, 3).duplicate()
	 * }
	 * </pre>
	 *
	 * @see LazyFutureStream#copy(int)
	 *
	 * @see #duplicate(Stream)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> duplicate() {
		List<LazyFutureStream<U>> duplicated = this.copy(2);
		return new Tuple2(duplicated.get(0), duplicated.get(1));
	}
	/*
	 * <pre>
	 * {@code 
	 * 
	 * // tuple((1, 2, 3), (1, 2, 3)) 
	 * 
	 * LazyFutureStream.of(1, 2, 3).duplicate()
	 * }
	 * </pre>
	 *
	 * @see LazyFutureStream#copy(int)
	 *
	 * @see #duplicate(Stream)
	 */
	default Tuple2<Seq<U>, Seq<U>> duplicateFutureStream() {
		Tuple2 duplicated = this.duplicate();
		return new Tuple2(duplicated.v1, duplicated.v2);
	}
	
	
	
	
	/**
	 * Partition a stream in two given a predicate. Two LazyFutureStreams are 
	 * returned but Seq interface specifies return type is Seq. See partitionFutureStream to
	 * see an alternative which returns LazyFutureStream
	 * 
	 * <code>
	 * 
	 * // tuple((1, 3, 5), (2, 4, 6)) 
	 * 
	 * LazyFutureStream.of(1, 2, 3, 4, 5,6).partition(i -&gt; i % 2 != 0)
	 * 
	 * </code>
	 * 
	 * @see #partitionFutureStream(Predicate)
	 * @see #partition(Stream, Predicate)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> partition(Predicate<? super U> predicate) {
		Tuple2<Seq<U>, Seq<U>> partitioned = FutureStream.super
				.partition(predicate);
		return new Tuple2(partitioned.v1, partitioned.v2);
	}
	/**
	 * Partition an LazyFutureStream into two LazyFutureStreams given a
	 * predicate.
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
	 * 
	 * results in
	 * 
	 * tuple((1, 3, 5), (2, 4, 6))
	 * }</pre>
	 * @param predicate
	 *            Predicate to split Stream
	 * @return LazyFutureStream
	 * @see #partition(Predicate)
	 */
	default Tuple2<LazyFutureStream<U>, LazyFutureStream<U>> partitionFutureStream(
			Predicate<? super U> predicate) {
		Tuple2 partition = partition(predicate);
		return (Tuple2<LazyFutureStream<U>, LazyFutureStream<U>>) partition;
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
	@Override
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
	@Override
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
     * Cross join 2 streams into one.
     * <p>
     * <pre>{@code 
     * // (tuple(1, "a"), tuple(1, "b"), tuple(2, "a"), tuple(2, "b"))
     * LazyFutureStream.of(1, 2).crossJoin(LazyFutureStream.of("a", "b"))
     * }</pre>
     */
	default <T> LazyFutureStream<Tuple2<U, T>> crossJoin(Stream<T> other) {
	     return (LazyFutureStream)FutureStream.super.crossJoin(other);
	}
	
	/**
     * Produce this stream, or an alternative stream from the
     * {@code value}, in case this stream is empty.
     */
	default LazyFutureStream<U> onEmpty(U value){
		
		return (LazyFutureStream)FutureStream.super.onEmpty(value);
	}
	/**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	default LazyFutureStream<U> onEmptyGet(Supplier<U> supplier){
		return (LazyFutureStream)FutureStream.super.onEmptyGet(supplier);
	}
	/**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	default <X extends Throwable> LazyFutureStream<U> onEmptyThrow(Supplier<X> supplier) {
			return (LazyFutureStream)FutureStream.super.onEmptyThrow(supplier);
	}
    /**
     * Inner join 2 streams into one.
     * 
     * <pre>
     * {@code 
     * // (tuple(1, 1), tuple(2, 2))
     * LazyFutureStream.of(1, 2, 3).innerJoin(Seq.of(1, 2), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> innerJoin(Stream<T> other, BiPredicate<U, T> predicate) {
    	return (LazyFutureStream)FutureStream.super.innerJoin(other,predicate);
       
    }

    /**
     * Left outer join 2 streams into one.
     * <p>
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2), tuple(3, null))
     * LazyFutureStream.of(1, 2, 3).leftOuterJoin(Seq.of(1, 2), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> leftOuterJoin(Stream<T> other, BiPredicate<U, T> predicate) {

    	return (LazyFutureStream)FutureStream.super.leftOuterJoin(other,predicate);
    }

    /**
     * Right outer join 2 streams into one.
     * <p>
     * <pre> 
     * {@code 
     * // (tuple(1, 1), tuple(2, 2), tuple(null, 3))
     * LazyFutureStream.of(1, 2).rightOuterJoin(Seq.of(1, 2, 3), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> rightOuterJoin(Stream<T> other, BiPredicate<U, T> predicate) {
        return  (LazyFutureStream)FutureStream.super.rightOuterJoin(other,predicate);
    }
    /**
	 * Create a Stream that infinitely cycles this Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(LazyFutureStream.of(1,2,2).cycle().limit(6)
								.collect(Collectors.toList()),
									equalTo(Arrays.asList(1,2,2,1,2,2));
	 * }
	 * </pre>
	 * @return New cycling stream
	 */
	@Override
	default LazyFutureStream<U> cycle(){
		RepeatableStream s = new RepeatableStream(ToLazyCollection.toLazyCollection(toQueue().stream(getSubscription()).iterator()));
		return fromStream(Stream.iterate(s.stream(),s1-> s.stream()).flatMap(i->i));
	}
	
	/**
	 * Create a Stream that finitely cycles this Stream, provided number of times
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(LazyFutureStream.of(1,2,2).cycle(3)
								.collect(Collectors.toList()),
									equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * @return New cycling stream
	 */
	default LazyFutureStream<U> cycle(int times){
		RepeatableStream s = new RepeatableStream(ToLazyCollection.toLazyCollection(toQueue().stream(getSubscription()).iterator()));
		return fromStream(Stream.iterate(s.stream(),s1-> s.stream()).limit(times).flatMap(i->i));
	}
	/**
	 * Repeat in a Stream while specified predicate holds
	 * <pre>
	 * {@code 
	 *  int count =0;
	 *  
		assertThat(LazyFutureStream.of(1,2,2).cycleWhile(next -> count++<6 )
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	default  LazyFutureStream<U> cycleWhile(Predicate<? super U> predicate) {
		return cycle().limitWhile(predicate);
	}

	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * 	count =0;
		assertThat(LazyFutureStream.of(1,2,2,3).cycleUntil(next -> count++>10 )
											.collect(Collectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));

	 * }
	 * </pre>
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	default  LazyFutureStream<U> cycleUntil(Predicate<? super U> predicate) {
		
		return cycle().limitUntil(predicate);
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
		return this.withAsync(true).withTaskExecutor(LazyReact.parallelBuilder().getExecutor());
	}
	/* 
	 *	@return New version of this stream  converted to execute synchronously and sequentially
	 * @see com.aol.simple.react.stream.traits.FutureStream#sequential()
	 */
	default LazyFutureStream<U> sequential(){
		return this.withAsync(false).withTaskExecutor(LazyReact.sequentialBuilder().getExecutor());
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
		getLastActive().stream().onClose(closeHandler);
		return this;
		
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

	/**
	 * Construct an parallel LazyFutureStream from specified array, using the configured
	 * standard parallel thread pool. By default this is the Common ForkJoinPool. 
	 * To use a different thread pool, the recommended approach is to construct your own LazyReact builder 
	 * 
	 * @see ThreadPools#getStandard()
	 * @see ThreadPools#setUseCommon(boolean)
	 * 
	 * @param array
	 *            Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> LazyFutureStream<U> parallel(U... array) {
		return  LazyReact.parallelCommonBuilder().of(array);
	}

	

	
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on the supplied CompletableFutures executor service (unless async operator invoked
	 *  , in which it will switch to the common 'free' thread executor)
	 *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
	 *  
	 * 
	 */
	static <T> LazyFutureStream<T> lazyFutureStreamFrom(Stream<CompletableFuture<T>> stream) {
		return  new LazyReact(ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(ThreadPools.getSequentialRetry())).withAsync(false)
									.fromStream(stream);
	}
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on the supplied CompletableFutures executor service (unless async operator invoked
	 *  , in which it will switch to the common 'free' thread executor)
	 *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
	 *  
	 *
	 */
	static <T> LazyFutureStream<T> lazyFutureStream(CompletableFuture<T> value) {
		return  new LazyReact(ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(ThreadPools.getSequentialRetry())).withAsync(false)
									.fromStream(Stream.of(value));
	}
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
	 *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
	 *  is invoked.
	 *  
	 * 
	 */
	static <T> LazyFutureStream<T> lazyFutureStream(CompletableFuture<T>... values) {
		return  new LazyReact(ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(ThreadPools.getSequentialRetry())).withAsync(false)
									.fromStream(Stream.of(values));
	}


	/**
	 *  Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
	 *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
	 *  is invoked.
	 *  
	 * @see Stream#of(Object)
	 */
	static <T> LazyFutureStream<T> react(Supplier<T> value) {
		return  new LazyReact(ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(ThreadPools.getSequentialRetry())).withAsync(false).react(value);
	}

	/**
	 Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
	 * The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator is invoked.

	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> react(Supplier<T>... values) {
		return  new LazyReact(ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(ThreadPools.getSequentialRetry())).withAsync(false).react(values);
	}
	/**
	 * Create a sequential synchronous stream that runs on the current thread
	 * 
	 */
	static <T> LazyFutureStream<T> of(T value) {
		return lazyFutureStream((Stream) Seq.of(value));
	}

	/**
	 * Create a sequential synchronous stream that runs on the current thread
	 * 
	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> of(T... values) {
		return lazyFutureStream((Stream) Seq.of(values));
	}
	/**
	 * Create a sequential synchronous stream that runs on a free thread (commonFreeThread executor by default, shared
	 * across any instances created in this manner. 
	 * @see com.aol.simple.react.stream.ThreadPools#setUseCommon(boolean) 
	 * 
	 */
	static <T> LazyFutureStream<T> ofThread(T value) {
		return (LazyFutureStream)ofThread(new Object[]{value});
	}

	/**
	 * Create a sequential synchronous stream that runs on a free thread (commonFreeThread executor by default, shared
	 * across any instances created in this manner. 
	 * @see com.aol.simple.react.stream.ThreadPools#setUseCommon(boolean) 
	 * 
	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> ofThread(T... values) {
		LazyReact react =new LazyReact(ThreadPools.getSequential(), RetryBuilder.getDefaultInstance()
				.withScheduler(ThreadPools.getSequentialRetry()),false, new MaxActive(1,1));
		return new LazyFutureStreamImpl<T>(react, Stream.of(values));
		
	}

	/**
	 * Create a sequential synchronous stream that runs on the current thread
	 */
	static <T> LazyFutureStream<T> empty() {
		return lazyFutureStream((Stream) Seq.empty());
	}

	/**
	 * @see Stream#iterate(Object, UnaryOperator)
	 */
	static <T> LazyFutureStream<T> iterate(final T seed,
			final UnaryOperator<T> f) {
		return lazyFutureStream((Stream) Seq.iterate(seed, f));
	}

	/**
	 * Generate an infinite Stream of null values that runs on the current thread
	 * @see Stream#generate(Supplier)
	 */
	static LazyFutureStream<Void> generate() {
		return generate(() -> null);
	}

	/**
	 * Generate an infinite Stream of given value that runs on the current thread
	 * @see Stream#generate(Supplier)
	 */
	static <T> LazyFutureStream<T> generate(T value) {
		return generate(() -> value);
	}

	/**
	 * Generate an infinite Stream of value returned from Supplier that runs on the current thread
	 * @see Stream#generate(Supplier)
	 */
	static <T> LazyFutureStream<T> generate(Supplier<T> s) {
		return lazyFutureStream(Stream.generate(s));
	}

	/**
	 * Wrap a Stream into a FutureStream that runs on the current thread
	 */
	static <T> LazyFutureStream<T> lazyFutureStream(Stream<T> stream) {
		if (stream instanceof LazyFutureStream)
			return (LazyFutureStream<T>) stream;
		if (stream instanceof FutureStream)
			stream = ((FutureStream) stream).toQueue().stream(
					((FutureStream) stream).getSubscription());
		LazyReact react =new LazyReact(ThreadPools.getCurrentThreadExecutor(), RetryBuilder.getDefaultInstance()
						.withScheduler(ThreadPools.getSequentialRetry()),false, new MaxActive(1,1));
		return new LazyFutureStreamImpl<T>(react,
				stream);
		
	}

	/**
	 * Wrap an Iterable into a FutureStream that runs on the current thread
	 */
	static <T> LazyFutureStream<T> lazyFutureStreamFromIterable(Iterable<T> iterable) {
		return lazyFutureStream(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream that runs on the current thread
	 */
	static <T> LazyFutureStream<T> lazyFutureStream(Iterator<T> iterator) {
		return lazyFutureStream(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}
	
	

}
