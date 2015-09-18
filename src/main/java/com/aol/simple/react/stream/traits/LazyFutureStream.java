package com.aol.simple.react.stream.traits;

import static com.aol.simple.react.stream.traits.NullValue.NULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
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
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.HotStream;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.streams.HotStreamImpl;
import com.aol.cyclops.streams.future.FutureOperationsImpl;
import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.Queue.QueueTimeoutException;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.reactivestreams.FutureStreamAsyncPublisher;
import com.aol.simple.react.reactivestreams.FutureStreamSynchronousPublisher;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.lazy.LazyFutureStreamImpl;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.future.operators.ToLazyCollection;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */

public interface LazyFutureStream<U> extends  LazySimpleReactStream<U>,LazyStream<U>,
											SequenceM<U>,Seq<U>, LazyToQueue<U>,
										ConfigurableStream<U,FastFuture<U>>,
										FutureStreamAsyncPublisher<U>,
										FutureStreamSynchronousPublisher<U> {

	default CloseableIterator<U> iterator() {
		return (CloseableIterator)LazySimpleReactStream.super.iterator();
	}
	Continueable getSubscription();
	void cancel();
	<R> LazyFutureStream<R> withLastActive(LazyStreamWrapper<R> streamWrapper);
	LazyReact getSimpleReact();
	default void  subscribe(Subscriber<? super U> s){
		if(isAsync())
			FutureStreamAsyncPublisher.super.subscribe(s);
		else
			FutureStreamSynchronousPublisher.super.subscribe(s);
	}
	
	/**
	 * @return an Iterator that chunks all completed elements from this stream since last it.next() call into a collection
	 */
	default Iterator<Collection<U>> chunkLastReadIterator(){
		
		Queue.QueueReader reader =  new Queue.QueueReader(this.withQueueFactory(QueueFactories.unboundedQueue())
																	.toQueue(q->q.withTimeout(100)
																	.withTimeUnit(TimeUnit.MICROSECONDS))
																	,null);
		class Chunker implements Iterator<Collection<U>> {
			volatile boolean open =true;
			@Override
			public boolean hasNext() {

				return open == true && reader.isOpen();
			}

			@Override
			public Collection<U> next() {
				
				while(hasNext()){
					try{
						return reader.drainToOrBlock();
					}catch(ClosedQueueException e){
						open =false;
						return new ArrayList<>();
					}catch(QueueTimeoutException e){
						LockSupport.parkNanos(0l);
					}
				}
				return new ArrayList<>();
				

			}
		}
		return new Chunker();
	}
	/**
	 * @return a Stream that batches all completed elements from this stream since last read attempt into a collection
	 */
	default LazyFutureStream<Collection<U>> chunkSinceLastRead(){
		Queue queue = this.withQueueFactory(QueueFactories.unboundedQueue()).toQueue();
		Queue.QueueReader reader =  new Queue.QueueReader(queue,null);
		class Chunker implements Iterator<Collection<U>> {
			
			@Override
			public boolean hasNext() {

				return reader.isOpen();
			}

			@Override
			public Collection<U> next() {
				return reader.drainToOrBlock();

			}
		}
		Chunker chunker = new Chunker();
		Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
			return () -> {
				
				try {
					return chunker.next();
				} catch (ClosedQueueException e) {
					
					throw new ClosedQueueException();
				}
				
			};
		};
		return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
		
		
	}
	
	//terminal operations
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#count()
	 */
	@Override
	default long count(){
		//performance optimisation count the underlying futures
		return getLastActive().stream().count();
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
				.(e -> error = e)
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


	LazyFutureStream<U> withLazyCollector(Supplier<LazyResultConsumer<U>> lazy);
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

	
	LazyFutureStream<U> withSubscription(Continueable sub);

	
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
		return null;
	}
	default <R> LazyFutureStream<R> thenSync(final Function<U, R> fn){
		return (LazyFutureStream<R>)LazySimpleReactStream.super.thenSync(fn);
	}
	default  LazyFutureStream<U> peekSync(final Consumer<? super U> consumer) {
		return (LazyFutureStream<U>)LazySimpleReactStream.super.peekSync(consumer);
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
	default EagerSimpleReactStream<U> convertToSimpleReact(){
		return new SimpleReact(getTaskExecutor()).withRetrier(getRetrier()).fromStream((Stream)getLastActive()
																		.injectFutures()
																		.map(f-> {
																			try{
																				return CompletableFuture.completedFuture(f.join());
																			}catch(Throwable t){
																				return new CompletableFuture().completeExceptionally(t);
																			}
																		}));
																		
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
		if(other instanceof LazyFutureStream)
			return zipFutures((LazyFutureStream)other);
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

	default <R> LazyFutureStream<Tuple2<U,R>> zipFutures(LazyFutureStream<R> other) {
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

		toQueue(shards, sharder);
		return shards
				.entrySet()
				.stream()
				.collect(
						Collectors.toMap(e -> e.getKey(), e -> fromStream(e
								.getValue().stream(getSubscription()))));
	}

	/**
	 * Can be used to debounce (accept a single data point from a unit of time)
	 * data. This drops data. For a method that slows emissions and keeps data
	 * #see#onePer
	 * 
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1,2,3,4,5,6)
	 *          .debounce(1000,TimeUnit.SECONDS).toList();
	 *          
	 * // [1] 
	 * }</pre>
	 * 
	 * @param time
	 *            Time from which to accept only one element
	 * @param unit
	 *            Time unit for specified time
	 * @return Next stage of stream, with only 1 element per specified time
	 *         windows
	 */
	default LazyFutureStream<U> debounce(long time, TimeUnit unit) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).debounce(time, unit));
		
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
	default LazyFutureStream<U> control(Function<Supplier<U>, Supplier<U>> fn){
		Queue queue = toQueue();
		return fromStream(queue.streamControl(getSubscription(), fn));
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
	default <C extends Collection<U>>LazyFutureStream<C> batch(Function<Supplier<U>, Supplier<C>> fn){
		Queue queue = toQueue();
		return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).batchBySize(size));

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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).batchBySizeAndTime(size,time,unit));

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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).batchBySize(size,supplier));

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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).jitter(jitterInNanos));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).fixedDelay(time,unit));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).onePer(time,unit));

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
	default LazyFutureStream<U> xPer(int x, long time, TimeUnit unit) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).xPer(x,time,unit));
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
	default LazyFutureStream<List<U>> batchByTime(long time, TimeUnit unit) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).batchByTime(time,unit));
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
	default  <C extends Collection<U>> LazyFutureStream<C> batchByTime(long time,
			TimeUnit unit, Supplier<C> factory) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).batchByTime(time,unit,factory));

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
		return (LazyFutureStream) LazySimpleReactStream.super.then(fn);
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
			Function<? super U,CompletableFuture<? extends R>> flatFn) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).flatMapCompletableFuture(flatFn));
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
	default <R> LazyFutureStream<R> flatMapToCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn) {
		
		return (LazyFutureStream) LazySimpleReactStream.super.flatMapToCompletableFutureSync(flatFn);
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
		return fromStream(Stream.concat(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
				false),StreamSupport.stream(Spliterators.spliteratorUnknownSize(other.iterator(), Spliterator.ORDERED),
						false)));
		
		
		
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
		return (LazyFutureStream)LazySimpleReactStream.super.cast(type);
		
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
		return (LazyFutureStream)LazySimpleReactStream.super.ofType(type);
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
		return (LazyFutureStream)LazySimpleReactStream.super.intersperse(value);
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
		return (LazyFutureStream)fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).sliding(size));
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
		return (LazyFutureStream)fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).sliding(size,increment));
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
		return SequenceM.super.duplicate();
	
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
	default Tuple2<LazyFutureStream<U>, LazyFutureStream<U>> duplicateFutureStream() {
		Tuple2<Seq<U>,Seq<U>> duplicated = this.duplicate();
		return new Tuple2(fromStream(duplicated.v1), fromStream(duplicated.v2));
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
		return SequenceM.super.partition(predicate);	
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
		Tuple2<Seq<U>,Seq<U>> partition = partition(predicate);
		return new Tuple2(fromStream(partition.v1), fromStream(partition.v2));
	}

	@Override
	default LazyFutureStream<U> slice(long from, long to) {
		
		return fromStream(fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.slice(from, to)));
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
		return fromStream(fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zipWithIndex()));
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
		return fromStream(fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).scanLeft(seed, function)));
		
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
		return fromStream(fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).scanRight(seed, function)));
		
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).reverse());
		
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).shuffle());
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).shuffle(random));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).skipWhile(predicate));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).skipUntil(predicate));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).limitWhile(predicate));
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).crossJoin(other));
	}
	
	/**
     * Produce this stream, or an alternative stream from the
     * {@code value}, in case this stream is empty.
     */
	default LazyFutureStream<U> onEmpty(U value){
		
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).onEmpty(value));
	}
	/**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	default LazyFutureStream<U> onEmptyGet(Supplier<U> supplier){
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).onEmptyGet(supplier));
	}
	/**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	default <X extends Throwable> LazyFutureStream<U> onEmptyThrow(Supplier<X> supplier) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).onEmptyThrow(supplier));
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
    	return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).innerJoin(other,predicate));
       
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
    	return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).leftOuterJoin(other,predicate));
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
    	return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).rightOuterJoin(other,predicate));
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
		return toQueue().stream(getSubscription());
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
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).sorted());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	default LazyFutureStream<U> sorted(Comparator<? super U> comparator) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).sorted(comparator));
	}

	/**
	 * Give a consumer access to this Stream
	 * 
	 * @param consumer
	 *            Consumer that will recieve current stage
	 * @return Self (current stage)
	 */
	default LazyFutureStream<U> self(Consumer<LazyFutureStream<U>> consumer) {
		return  then((t) -> {
			consumer.accept(this);
			return (U) t;
		});

	}

	/** START SEQUENCEM **/
	@Override
	default <R> R unwrap() {
		return (R)this;
	}

	
	@Override
	default <T1>  LazyFutureStream<T1> flatten() {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).flatten());

	}

	@Override
	default Optional<List<U>> toOptional() {
		return Optional.of(block());
	}

	@Override
	default CompletableFuture<List<U>> toCompletableFuture() {
		return CompletableFuture.completedFuture(block());
	}

	


	@Override
	default Spliterator<U> spliterator() {
		return stream().spliterator();
	}

	@Override
	default boolean isParallel() {
		return false;
	}

	@Override
	default IntStream mapToInt(ToIntFunction<? super U> mapper) {
		return stream().mapToInt(mapper);
	}

	@Override
	default LongStream mapToLong(ToLongFunction<? super U> mapper) {
		return stream().mapToLong(mapper);
	}

	@Override
	default DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) {
		return stream().mapToDouble(mapper);
	}

	@Override
	default IntStream flatMapToInt(
			Function<? super U, ? extends IntStream> mapper) {
		return stream().flatMapToInt(mapper);
	}

	@Override
	default LongStream flatMapToLong(
			Function<? super U, ? extends LongStream> mapper) {
		return stream().flatMapToLong(mapper);
	}

	@Override
	default DoubleStream flatMapToDouble(
			Function<? super U, ? extends DoubleStream> mapper) {
		return stream().flatMapToDouble(mapper);
	}

	@Override
	default void forEachOrdered(Consumer<? super U> action) {
		stream().forEachOrdered(action);
		
	}

	@Override
	default Object[] toArray() {
		return stream().toArray();
	}

	@Override
	default <A> A[] toArray(IntFunction<A[]> generator) {
		return stream().toArray(generator);
	}
	@Override
	default <K> Map<K, List<U>> groupBy(
			Function<? super U, ? extends K> classifier) {
		return Seq.seq(stream()).groupBy(classifier);
	}

	@Override
	default boolean allMatch(Predicate<? super U> c) {
		return SequenceM.fromStream(stream()).allMatch(c);
	}

	@Override
	default  boolean anyMatch(Predicate<? super U> c) {
		return SequenceM.fromStream(stream()).anyMatch(c);
	}

	@Override
	default boolean noneMatch(Predicate<? super U> c) {
		return SequenceM.fromStream(stream()).noneMatch(c);
	}

	@Override
	default String join() {
		return SequenceM.fromStream(stream()).join();
	}

	@Override
	default String join(String sep) {
		return SequenceM.fromStream(stream()).join(sep);
	}

	@Override
	default String join(String sep, String start, String end) {
		return SequenceM.fromStream(stream()).join(sep,start,end);
	}

	@Override
	default <C extends Comparable<C>> Optional<U> minBy(Function<U, C> f) {
		return SequenceM.fromStream(stream()).minBy(f);
	}

	@Override
	default Optional<U> min(Comparator<? super U> comparator) {
		return SequenceM.fromStream(stream()).min(comparator);
	}

	@Override
	default <C extends Comparable<C>> Optional<U> maxBy(Function<U, C> f) {
		return SequenceM.fromStream(stream()).maxBy(f);
	}

	@Override
	default Optional<U> max(Comparator<? super U> comparator) {
		return SequenceM.fromStream(stream()).max(comparator);
	}

	@Override
	default Optional<U> findAny() {
		return SequenceM.fromStream(stream()).findAny();
	}

	@Override
	default U foldLeft(U identity, BinaryOperator<U> accumulator) {
		return SequenceM.fromStream(stream()).foldLeft(identity, accumulator);
	}

	@Override
	default U foldRight(U identity, BinaryOperator<U> accumulator) {
		return SequenceM.fromStream(stream()).foldRight(identity,accumulator);
	}

	@Override
	default Set<U> toSet() {
		return SequenceM.fromStream(stream()).toSet();
	}

	@Override
	default List<U> toList() {
		return SequenceM.fromStream(stream()).toList();
	}

	@Override
	default <C extends Collection<U>> C toCollection(
			Supplier<C> collectionFactory) {
		return SequenceM.fromStream(stream()).toCollection(collectionFactory);
	}

	@Override
	default <R> SequenceM<U> distinct(
			Function<? super U, ? extends R> keyExtractor) {
		return SequenceM.fromStream(stream()).distinct();
	}


	@Override
	default Tuple2<SequenceM<U>, SequenceM<U>> duplicateSequence() {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).duplicateSequence();
	}

	@Override
	default Tuple3<SequenceM<U>, SequenceM<U>, SequenceM<U>> triplicate() {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).triplicate();

	}

	@Override
	default Tuple4<SequenceM<U>, SequenceM<U>, SequenceM<U>, SequenceM<U>> quadruplicate() {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).quadruplicate();

	}

	@Override
	default Tuple2<Optional<U>, SequenceM<U>> splitSequenceAtHead() {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).splitSequenceAtHead();

	}

	@Override
	default Tuple2<SequenceM<U>, SequenceM<U>> splitAt(int where) {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).splitAt(where);

	}

	@Override
	default Tuple2<SequenceM<U>, SequenceM<U>> splitBy(Predicate<U> splitter) {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).splitBy(splitter);

	}

	@Override
	default Tuple2<SequenceM<U>, SequenceM<U>> partitionSequence(
			Predicate<U> splitter) {
		return SequenceM.fromStream(toQueue()
							.stream(getSubscription()))
							.partitionSequence(splitter);

	}

	@Override
	default  LazyFutureStream<U> cycle(Monoid<U> m, int times) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).cycle(m,times));

	}

	@Override
	default <R> LazyFutureStream<R> cycle(Class<R> monadC, int times) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).cycle(monadC,times));

	}

	
	@Override
	default <R> LazyFutureStream<Tuple2<U, R>> zipStream(Stream<R> other) {
		return  (LazyFutureStream) fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zipStream(other));

	}

	
	@Override
	default <S, R> LazyFutureStream<Tuple3<U, S, R>> zip3(Stream<? extends S> second,
			Stream<? extends R> third) {
		return  (LazyFutureStream)fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zip3(second,third));

	}

	@Override
	default <T2, T3, T4> LazyFutureStream<Tuple4<U, T2, T3, T4>> zip4(
			Stream<T2> second, Stream<T3> third, Stream<T4> fourth) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zip4(second,third,fourth));

	}

	

	@Override
	default <S, R> LazyFutureStream<R> zipSequence(SequenceM<? extends S> second,
			BiFunction<? super U, ? super S, ? extends R> zipper) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zipSequence(second,zipper));

	}

	@Override
	default <S, R> LazyFutureStream<R> zipAnyM(AnyM<? extends S> second,
			BiFunction<? super U, ? super S, ? extends R> zipper) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zipAnyM(second,zipper));

	}

	@Override
	default <S, R> LazyFutureStream<R> zipStream(
			BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second,
			BiFunction<? super U, ? super S, ? extends R> zipper) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription())).zipStream(second,zipper));

	}

	
	@Override
	default LazyFutureStream<List<U>> grouped(int groupSize) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
							.grouped(groupSize));

	}

	

	
	@Override
	default LazyFutureStream<U> scanLeft(Monoid<U> monoid) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.scanLeft(monoid));
	}

	

	@Override
	default LazyFutureStream<U> scanRight(Monoid<U> monoid) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.scanRight(monoid));
	}


	

	@Override
	default boolean xMatch(int num, Predicate<? super U> c) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.xMatch(num,c);
	}

	
	

	@Override
	default HeadAndTail<U> headAndTail() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.headAndTail();
	}

	@Override
	default Optional<HeadAndTail<U>> headAndTailOptional() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.headAndTailOptional();
	}

	

	

	@Override
	default <R> R mapReduce(Monoid<R> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.mapReduce(reducer);
	}

	@Override
	default <R> R mapReduce(Function<? super U, ? extends R> mapper,
			Monoid<R> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.mapReduce(mapper,reducer);
	}

	@Override
	default List collectStream(Stream<Collector> collectors) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.collectStream(collectors);
	}

	@Override
	default <R> List<R> collectIterable(Iterable<Collector> collectors) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.collectIterable(collectors);
	}

	@Override
	default U reduce(Monoid<U> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.reduce(reducer);
	}

	
	@Override
	default List<U> reduce(Stream<Monoid<U>> reducers) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.reduce(reducers);
	}

	@Override
	default List<U> reduce(Iterable<Monoid<U>> reducers) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.reduce(reducers);
	}

	@Override
	default U foldLeft(Monoid<U> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.foldLeft(reducer);
	}

	
	@Override
	default <T> T foldLeftMapToType(Monoid<T> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.foldLeftMapToType(reducer);
	}

	@Override
	default U foldRight(Monoid<U> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.foldRight(reducer);
	}

	
	@Override
	default <T> T foldRightMapToType(Monoid<T> reducer) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.foldRightMapToType(reducer);
	}

	@Override
	default Streamable<U> toStreamable() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.toStreamable();
	}

	

	@Override
	default <T> Stream<T> toStream() {
		return (Stream<T>)this;
	}

	

	@Override
	default boolean startsWith(Iterable<U> iterable) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.startsWith(iterable);
	}

	@Override
	default boolean startsWith(Iterator<U> iterator) {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.startsWith(iterator);
	}

	@Override
	default AnyM<U> anyM() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.anyM();
	}

	
	@Override
	default <R> LazyFutureStream<R> flatMapAnyM(
			Function<? super U, AnyM<? extends R>> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapAnyM(fn));
	}

	@Override
	default <R> LazyFutureStream<R> flatMapCollection(
			Function<? super U, Collection<? extends R>> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapCollection(fn));
	}

	@Override
	default <R> LazyFutureStream<R> flatMapStream(
			Function<? super U, BaseStream<? extends R, ?>> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapStream(fn));
	}

	@Override
	default <R> LazyFutureStream<R> flatMapOptional(
			Function<? super U, Optional<? extends R>> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapOptional(fn));
	}

	

	@Override
	default LazyFutureStream<Character> flatMapCharSequence(
			Function<? super U, CharSequence> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapCharSequence(fn));
	}

	@Override
	default LazyFutureStream<String> flatMapFile(Function<? super U, File> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapFile(fn));
	}

	@Override
	default LazyFutureStream<String> flatMapURL(Function<? super U, URL> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapURL(fn));
	}

	@Override
	default LazyFutureStream<String> flatMapBufferedReader(
			Function<? super U, BufferedReader> fn) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.flatMapBufferedReader(fn));
	}



	@Override
	default Collection<U> toLazyCollection() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.toLazyCollection();
	}

	@Override
	default Collection<U> toConcurrentLazyCollection() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.toConcurrentLazyCollection();
	}

	@Override
	default Streamable<U> toConcurrentLazyStreamable() {
		return  SequenceM.fromStream(toQueue().stream(getSubscription()))
				.toConcurrentLazyStreamable();
	}

	

	@Override
	default LazyFutureStream<U> appendStream(Stream<U> stream) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.appendStream(stream));
	}

	@Override
	default LazyFutureStream<U> prependStream(Stream<U> stream) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.prependStream(stream));
	}

	@Override
	default LazyFutureStream<U> append(U... values) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.append(values));
	}

	@Override
	default LazyFutureStream<U> prepend(U... values) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.prepend(values));
	}

	@Override
	default LazyFutureStream<U> insertAt(int pos, U... values) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.insertAt(pos,values));
	}

	@Override
	default LazyFutureStream<U> deleteBetween(int start, int end) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.deleteBetween(start,end));
	}

	@Override
	default LazyFutureStream<U> insertStreamAt(int pos, Stream<U> stream) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.insertStreamAt(pos,stream));
	}
	
	default FutureOperations<U> futureOperations() {
		return new FutureOperationsImpl<>(getTaskExecutor(), this);
		
	}
	@Override
	default FutureOperations<U> futureOperations(Executor exec) {
		return new FutureOperationsImpl<>(exec, this);
		
	}

	@Override
	default boolean endsWith(Iterable<U> iterable) {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).endsWith(iterable);
	}

	@Override
	default boolean endsWith(Stream<U> stream) {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).endsWith(stream);
	}

	@Override
	default LazyFutureStream<U> skip(long time, TimeUnit unit) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.skip(time,unit));
	}

	@Override
	default LazyFutureStream<U> limit(long time, TimeUnit unit) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.limit(time,unit));
	}

	@Override
	default LazyFutureStream<U> skipLast(int num) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.skipLast(num));
	}

	@Override
	default LazyFutureStream<U> limitLast(int num) {
		return  fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.limitLast(num));
	}

	@Override
	default HotStream<U> hotStream(Executor e) {
		return new HotStreamImpl(this).init(e);
	}
	default HotStream<U> hotStream() {
		return new HotStreamImpl(this).init(this.getTaskExecutor());
	}

	@Override
	default U firstValue() {
		return SequenceM.fromStream(toQueue().stream(getSubscription())).firstValue();
	}

	
	

	@Override
	default <C extends Collection<U>> LazyFutureStream<C> batchBySizeAndTime(int size,
			long time, TimeUnit unit, Supplier<C> factory) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.batchBySizeAndTime(size, time,unit, factory));
	}

	
	@Override
	default LazyFutureStream<Streamable<U>> windowBySizeAndTime(int maxSize,
			long maxTime, TimeUnit maxTimeUnit) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.windowBySizeAndTime(maxSize, maxTime, maxTimeUnit));
	}

	@Override
	default LazyFutureStream<Streamable<U>> windowWhile(Predicate<U> predicate) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.windowWhile(predicate));
	}

	@Override
	default LazyFutureStream<Streamable<U>> windowUntil(Predicate<U> predicate) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.windowUntil(predicate));
	}

	@Override
	default LazyFutureStream<Streamable<U>> windowStatefullyWhile(
			BiPredicate<Streamable<U>, U> predicate) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.windowStatefullyWhile(predicate));
	}

	@Override
	default LazyFutureStream<Streamable<U>> windowByTime(long time, TimeUnit t) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.windowByTime(time,t));
	}

	@Override
	default LazyFutureStream<List<U>> batchUntil(Predicate<U> predicate) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.batchUntil(predicate));
	}

	@Override
	default LazyFutureStream<List<U>> batchWhile(Predicate<U> predicate) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.batchWhile(predicate));
	}

	@Override
	default <C extends Collection<U>> LazyFutureStream<C> batchWhile(
			Predicate<U> predicate, Supplier<C> factory) {
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.batchWhile(predicate,factory));
	}

	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	@Override
	default <R extends Comparable<? super R>> LazyFutureStream<U> sorted(
			Function<? super U, ? extends R> function){
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.sorted(function));
	}
	@Override
	default <C extends Collection<U>> LazyFutureStream<C> batchUntil(
			Predicate<U> predicate, Supplier<C> factory) {
		
		return fromStream(SequenceM.fromStream(toQueue().stream(getSubscription()))
				.batchUntil(predicate,factory));
	}

	@Override
	default LazyFutureStream<U> recover(Function<Throwable, U> fn) {
		return this.onFail(e->fn.apply(e.getCause()));
		
	}

	@Override
	default <EX extends Throwable> LazyFutureStream<U> recover(
			Class<EX> exceptionClass, Function<EX, U> fn) {
		return this.onFail(exceptionClass,e->fn.apply((EX)e.getCause()));
	}



	
	/** END SEQUENCEM **/
	
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
	static <T> LazyFutureStream<T> freeThread(T value) {
		return (LazyFutureStream)freeThread(new Object[]{value});
	}

	/**
	 * Create a sequential synchronous stream that runs on a free thread (commonFreeThread executor by default, shared
	 * across any instances created in this manner. 
	 * @see com.aol.simple.react.stream.ThreadPools#setUseCommon(boolean) 
	 * 
	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> freeThread(T... values) {
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
		if (stream instanceof LazyFutureStream)
			stream = ((LazyFutureStream) stream).toQueue().stream(
					((LazyFutureStream) stream).getSubscription());
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
