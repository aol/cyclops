package com.aol.simple.react.stream.traits;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerFutureStreamImpl;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.operators.BatchBySize;
import com.aol.simple.react.stream.traits.operators.BatchByTimeAndSize;
import com.aol.simple.react.util.SimpleTimer;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * 
 * Static Factory methods for Simple React Flows
 * 
 * 
 * @author johnmcclean
 *
 */
public interface EagerFutureStream<U> extends FutureStream<U>, EagerToQueue<U> {
	/* 
	 * Convert this stream into an async / sync stream
	 * 
	 *	@param async true if aysnc stream
	 *	@return
	 * @see com.aol.simple.react.stream.traits.ConfigurableStream#withAsync(boolean)
	 */
	EagerFutureStream<U> withAsync(boolean async);
	/* 
	 * Change task executor for the next stage of the Stream
	 * 
	 * <pre>
	 * {@code
	 *  EagerFutureStream.of(1,2,3,4)
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
	EagerFutureStream<U> withTaskExecutor(Executor e);

	/* 
	 * Change the Retry Executor used in this stream for subsequent stages
	 * <pre>
	 * {@code
	 * List<String> result = new EagerReact().react(() -> 1)
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
	EagerFutureStream<U> withRetrier(RetryExecutor retry);

	
	EagerFutureStream<U> withWaitStrategy(Consumer<CompletableFuture> c);

	

	EagerFutureStream<U> withLazyCollector(LazyResultConsumer<U> lazy);

	/* 
	 * Change the QueueFactory type for the next phase of the Stream.
	 * Default for EagerFutureStream is an unbounded blocking queue, but other types 
	 * will work fine for a subset of the tasks (e.g. an unbonunded non-blocking queue).
	 * 
	 * <pre>
	 * {@code
	 * List<Collection<String>> collected = EagerFutureStream
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
	 */
	EagerFutureStream<U> withQueueFactory(QueueFactory<U> queue);

	EagerFutureStream<U> withErrorHandler(
			Optional<Consumer<Throwable>> errorHandler);

	EagerFutureStream<U> withSubscription(Continueable sub);

	/*
	 * Synchronous version of then (executed on completing thread, without involving an Executor)
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
	default <R> EagerFutureStream<R> thenSync(final Function<U, R> fn){
		 return (EagerFutureStream<R>)FutureStream.super.thenSync(fn);
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
	default EagerFutureStream<U> sync(){
		return (EagerFutureStream<U>)FutureStream.super.sync();
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
	default EagerFutureStream<U> async(){
		return (EagerFutureStream<U>)FutureStream.super.async();
	}

	
	/**
	 * Convert between an Eager and Lazy future stream,
	 * can be used to take advantages of each approach during a single Stream
	 * 
	 * @return A LazyFutureStream from this EagerFutureStream
	 */
	default LazyFutureStream<U> convertToLazyStream(){
		return new LazyReact(getTaskExecutor()).withRetrier(getRetrier()).fromStream((Stream)getLastActive().stream());
	}
	/* 
	 * Apply a function to all items in the stream.
	 * <pre>
	 * {@code
	 *  EagerFutureStream.sequentialBuilder().react(()->1,()->2,()->3)
		 									 .map(it->it+100) //add 100
		 									 .toList();

	 * }
	 * //results in [100,200,300]
	 * </pre>
	 *	@param mapper Function to be applied to all items in the Stream
	 *	@return
	 * @see com.aol.simple.react.stream.traits.FutureStream#map(java.util.function.Function)
	 */
	default <R> EagerFutureStream<R> map(Function<? super U, ? extends R> mapper) {
		return (EagerFutureStream<R>)FutureStream.super.map(mapper);
	}
	
	/**
	 * @return a Stream that batches all completed elements from this stream
	 *         since last read attempt into a collection
	 */
	default EagerFutureStream<Collection<U>> chunkSinceLastRead() {
		return (EagerFutureStream<Collection<U>>) FutureStream.super
				.chunkSinceLastRead();
	}

	/**
	 * Break a stream into multiple Streams based of some characteristic of the
	 * elements of the Stream
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *  Queue<Integer> evenQueue = QueueFactories.unboundedQueue().build();
	 *   Queue<Integer> oddQueue = QueueFactories.unboundedQueue().build()
	 *  EagerFutureStream.of(10,20,25,30,41,43)
	 * 					 .shard(ImmutableMap.of("even",evenQueue,"odd",oddQueue),element-> element==0? "even" : "odd");
	 * 
	 * 
	 * Stream<Integer> evenStream = evenQueue.stream();
	 * }
	 * </pre>
	 * 
	 * 
	 * results in 2 Streams "even": 10,20,30 "odd" : 25,41,43
	 * 
	 * @param shards
	 *            Map of Queue's keyed by shard identifier
	 * @param sharder
	 *            Function to split split incoming elements into shards
	 * @return Map of new sharded Streams
	 */
	default <K> Map<K, EagerFutureStream<U>> shard(Map<K, Queue<U>> shards,
			Function<U, K> sharder) {
		Map map = FutureStream.super.shard(shards, sharder);
		return (Map<K, EagerFutureStream<U>>) map;
	}

	/**
	 * Cancel the CompletableFutures in this stage of the stream and the initial
	 * phase
	 */
	default void cancel() {
		cancelOriginal();
		FutureStream.super.cancel();

	}

	/**
	 * Cancel the original tasks that populated the EagerFuturestream
	 */
	void cancelOriginal();

	/**
	 * Can be used to debounce (accept a single data point from a unit of time)
	 * data. This drops data. For a method that slows emissions and keeps data
	 * @see LazyFutureStream#onePer(long, TimeUnit)
	 * 
	 * <pre>
	 * {@code 
	 * 
	 * 			EagerFutureStream.of(1,2,3,4,5,6)
	 * 							.debounce(1000,TimeUnit.SECONDS)
	 * 							.toList();
	 *   //[1] - as all events will be passed through in < 1000 seconds, only 1 will be allowed
	 * }
	 * 
	 * 
	 * </pre>
	 * 
	 * @param time
	 *            Time from which to accept only one element
	 * @param unit
	 *            Time unit for specified time
	 * @return Next stage of stream, with only 1 element per specified time
	 *         windows
	 */
	default EagerFutureStream<U> debounce(long time, TimeUnit unit) {
		long[] last = {0l};
		return this.filter(next-> {synchronized(last){
					if(System.nanoTime()-last[0]>unit.toNanos(time)){ 
						last[0]= System.nanoTime(); 
					return true;
					} 
					return false;}
		});
		
		
	}
	
	/**
	 * Return a Stream with the same values as this Stream, but with all values
	 * omitted until the provided stream starts emitting values. Provided Stream
	 * ends the stream of values from this stream.
	 <pre>
	  {@code 
	 * 			EagerFutureStream.react(()->loadNext(1),()->loadNext(2),()->3,()->4,()->loadNext(4))
	 * 							 .skipUntil(EagerFutureStream.react(()->loadFromDb()))
	 * 							 .toList();
	 * }
	 * </pre>
	 * @param s
	 *            Stream that will start the emission of values from this stream
	 * @return Next stage in the Stream but with all values skipped until the
	 *         provided Stream starts emitting
	 */
	default <T> EagerFutureStream<U> skipUntil(FutureStream<T> s) {
		return (EagerFutureStream<U>) FutureStream.super.skipUntil(s);
	}

	/**
	 * Return a Stream with the same values, but will stop emitting values once
	 * the provided Stream starts to emit values. e.g. if the provided Stream is
	 * asynchronously refreshing state from some remote store, this stream can
	 * proceed until the provided Stream succeeds in retrieving data.
	 * 
	 * <pre>
	 * {@code 
	 * 			EagerFutureStream.react(()->loadNext(1),()->loadNext(2),()->3,()->4,()->loadNext(4))
	 * 							 .takeUntil(EagerFutureStream.react(()->loadFromDb()))
	 * 							 .toList();
	 * }
	 * 
	 * </pre>
	 * 
	 * @param s
	 *            Stream that will stop the emission of values from this stream
	 * @return Next stage in the Stream but will only emit values until provided
	 *         Stream starts emitting values
	 */
	default <T> EagerFutureStream<U> takeUntil(FutureStream<T> s) {
		return (EagerFutureStream<U>) FutureStream.super.takeUntil(s);
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
	default EagerFutureStream<U> control(Function<Supplier<U>, Supplier<U>> fn) {
		return (EagerFutureStream<U>) FutureStream.super.control(fn);
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default EagerFutureStream<List<U>> batchBySizeAndTime(int batchSize,long time, TimeUnit unit) { 
		int size = this.getLastActive().list().size();
		List[] list = {new ArrayList<U>()};
		int[] count = {0};
		SimpleTimer[] timer = {new SimpleTimer()};
		return (EagerFutureStream)this.map(next-> { 
				synchronized(timer){
					count[0]++;
					if(unit.toNanos(time)-timer[0].getElapsedNanoseconds()>0 && list[0].size()+1<batchSize){
						
						list[0].add(next);
						
					} else{
						list[0].add(next);
						
						List<U> result = list[0];
						list[0] = new ArrayList<U>();
						 timer[0] = new SimpleTimer();
						return result;
						
					}
					if(count[0]==size){
						list[0].add(next);
						
						List<U> result = list[0];
						list[0] = new ArrayList<U>();
						return result;
					}
					return new ArrayList();
				}
			}	).filter(l->l.size()>0);
	}
	/**
	 * Organise elements in a Stream into a Collections based on the time period they pass through this stage
	 * This version uses locks - for a lock free implementation choose 
	 * @see LazyFutureStream#batchByTime(long, TimeUnit)
	 * 
	 * Will always include the next value over the batch time in current batch (again @see LazyFutureStream#batchByTime(long, TimeUnit)
	 * for a more powerful alternative that does not).
	 * 
	 * @param time Time period during which all elements should be collected
	 * @param unit Time unit during which all elements should be collected
	 * @return Stream of Lists
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default EagerFutureStream<Collection<U>> batchByTime(long time, TimeUnit unit) {
	
		int size = this.getLastActive().list().size();
		List[] list = {new ArrayList<U>()};
		int[] count = {0};
		SimpleTimer[] timer = {new SimpleTimer()};
		return (EagerFutureStream)this.map(next-> { 
				synchronized(timer){
					count[0]++;
					if(unit.toNanos(time)-timer[0].getElapsedNanoseconds()>0){
						
						list[0].add(next);
					} else{
						list[0].add(next);
						List<U> result = list[0];
						list[0] = new ArrayList<U>();
						 timer[0] = new SimpleTimer();
						return result;
						
					}
					if(count[0]==size){
						list[0].add(next);
						
						List<U> result = list[0];
						list[0] = new ArrayList<U>();
						return result;
					}
					return new ArrayList();
				}
			}	).filter(l->l.size()>0);
		
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
	default <C extends Collection<U>> EagerFutureStream<C> batch(
			Function<Supplier<U>, Supplier<C>> fn) {
		return (EagerFutureStream<C>) FutureStream.super.batch(fn);
	}

	/**
	 * 
	 * Batch the elements in this stream into Lists of specified size
	 * <pre>
	 * {@code
	 * EagerFutureStream.of(1,2,3,4,5,6)
	 * 					.batchBySize(3)
	 * 					.toList();
	 *
	 *  // [[1,2,3],[4,5,6]]
	 * }
	 *
	 * </pre>
	 * @param size
	 *            Size of lists elements should be batched into
	 * @return Stream of Lists
	 */
	default EagerFutureStream<List<U>> batchBySize(int size) {
		return batchBySize(size,()->new ArrayList<>());

	}
	

	/**
	 * Batch the elements in this stream into Collections of specified size The
	 * type of Collection is determined by the specified supplier
	 * 
	 * <pre>
	 * {@code 
	 * 		EagerFutureStream.of(1,1,1,1,1,1)
	 * 						.batchBySize(3,()->new TreeSet<>())
	 * 						.toList()
	 * 
	 *   //[[1],[1]]
	 * }
	 * 
	 * </pre>
	 * @param size
	 *            Size of batch
	 * @param supplier
	 *            Create the batch holding collection
	 * @return Stream of Collections
	 */
	default <C extends Collection<U>> EagerFutureStream<C> batchBySize(int size,
			Supplier<C> supplier) {
		Queue<U>  queue = toQueue();
		Function<Supplier<U>, Supplier<Optional<C>>> fn = new BatchBySize<U,C>(size,this.getSubscription(),queue, supplier)
																	.liftOptional();
		 
		EagerFutureStream<C> stream = this.async() 
											.map(u-> {synchronized(queue){return fn.apply(()-> queue.poll(1,TimeUnit.MICROSECONDS)).get();}})
											.filter(Optional::isPresent)
											.map(Optional::get);
		 if(this.isAsync())
				return stream.async();
			return stream;

	}

	/**
	 * Introduce a random delay between events in a stream Can be used to
	 * prevent behaviour synchronizing within a system
	 * EagerFutureStreams will batch results before jittering
	 * 
	 * For a better implementation see  @see LazyFutureStream#jitter(long)
	 * 
	 * <pre>
	 * {@code
	 * 
	 * EagerFutureStream.parallelCommonBuilder()
						.of(IntStream.range(0, 100))
						.map(it -> it*100)
						.jitter(10l)
						.peek(System.out::println)
						.block();
	 * 
	 * }
	
	 * </pre>
	 * @param jitterInNanos
	 *            Max number of nanos for jitter (random number less than this
	 *            will be selected)/
	 * @return Next stage in Stream with jitter applied
	 */
	default EagerFutureStream<U> jitter(long jitterInNanos) {
		return (EagerFutureStream<U>) FutureStream.super.jitter(jitterInNanos);
	}

	/**
	 * Apply a fixed delay before emitting elements to the next phase of the
	 * Stream. Note this doesn't neccessarily imply a fixed delay between
	 * element creation (although it may do). e.g.
	 * 
	 * <pre>
	 * {@code
	  	EagerFutureStream.of(1,2,3,4)
	  					.fixedDelay(1,TimeUnit.hours);
	  	}
	 * </pre>
	 * Will emit 1 on start, then 2 after an hour, 3 after 2 hours and so on.
	 * 
	 * However all 4 numbers will be populated in the Stream immediately.
	  
	   <pre>
	   {@code 
	 	LazyFutureStream.of(1,2,3,4)
	 					.withQueueFactories(QueueFactories.boundedQueue(1))
	 					.fixedDelay(1,TimeUnit.hours);
	 	}
	 	</pre>
	  
	 * Will populate each number in the Stream an hour apart.
	 * 
	 * @param time
	 *            amount of time between emissions
	 * @param unit
	 *            TimeUnit for emissions
	 * @return Next Stage of the Stream
	 */
	default EagerFutureStream<U> fixedDelay(long time, TimeUnit unit) {
		return (EagerFutureStream<U>) FutureStream.super.fixedDelay(time, unit);
	}

	/**
	 * Slow emissions down, emiting one element per specified time period
	 * 
	 * <pre>
	 * {@code 
	 * 		EagerFutureStream.of(1,2,3,4,5,6)
	 * 						 .onePer(1000,TimeUnit.NANOSECONDS)
	 * 						 .toList();
	 * 
	 * }
	 * 
	 * </pre>
	 * @param time
	 *            Frequency period of element emission
	 * @param unit
	 *            Time unit for frequency period
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	default EagerFutureStream<U> onePer(long time, TimeUnit unit) {
		return (EagerFutureStream<U>) FutureStream.super.onePer(time, unit);

	}

	/**
	 * Allows x (specified number of) emissions with a time period before
	 * stopping emmissions until specified time has elapsed since last emission
	 * 
	 * <pre>
	 * {@code 
	 *    EagerFutureStream.of(1,2,3,4,5,6)
	 *    				   .xPer(6,100000000,TimeUnit.NANOSECONDS)
	 *    				   .toList();
	 * 
	 * }
	 * 
	 * </pre>
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
		return (EagerFutureStream<U>) FutureStream.super.xPer(x, time, unit);
	}

	

	/**
	 * Similar to zip and withLatest, except will always take the latest from
	 * either Stream (merged with last available from the other). By contrast
	 * zip takes new / latest values from both Streams and withLatest will
	 * always take the latest from this Stream while taking the last available
	 * value from the provided stream.
	 * 
	 * <pre>
	 * {@code
	 *    EagerFutureStream.of(1,2,3,4,5,6)
	 *    				   .combineLatest(react(()->3,()->value()))
	 *    				   .toList();
	 * }
	 * </pre>
	 * @param s
	 *            Stream to merge with
	 * @return Stream of Tuples with the latest values from either stream
	 */
	default <T> EagerFutureStream<Tuple2<U, T>> combineLatest(FutureStream<T> s) {
		return (EagerFutureStream<Tuple2<U, T>>) FutureStream.super
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
	 * <pre>
	 * {@code
	 * EagerFutureStream.of(1,2,3,4,5,6)
	 * 					.withLatest(of(30,40,50,60,70,80,90,100,110,120,140))
						.toList();
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param s
	 *            Stream to merge with
	 * @return Stream of Tuples with the latest values from this stream
	 */
	default <T> EagerFutureStream<Tuple2<U, T>> withLatest(FutureStream<T> s) {
		return (EagerFutureStream<Tuple2<U, T>>) FutureStream.super
				.withLatest(s);
	}

	/**
	 * Return first Stream out of provided Streams that starts emitted results 
	 * 
	 * <pre>
	 * {@code 
	 * 	 EagerFutureStream.firstOf(stream1, stream2, stream3)
						.peek(System.out::println)
						.map(this::saveData)
						.block();
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param futureStreams Streams to race
	 * @return First Stream to start emitting values
	 */
	static <U> EagerFutureStream<U> firstOf(
			EagerFutureStream<U>... futureStreams) {
		return (EagerFutureStream<U>) FutureStream.firstOf(futureStreams);
	}

	/*
	 * React to new events with the supplied function on the supplied
	 * Executor
	 * 
	 * @param fn Apply to incoming events
	 * 
	 * @param service Service to execute function on
	 * 
	 * @return next stage in the Stream
	 */
	default <R> EagerFutureStream<R> then(final Function<U, R> fn,
			Executor service) {
		return (EagerFutureStream<R>) FutureStream.super.then(fn, service);
	}

	/**
	 * Can only be used on Eager Streams
	 * 
	 * Applies a function to this phase independent on the main flow.
	 * Convenience over taking a reference to this phase and splitting it.
	 * 
	 * 
	 * @param fn
	 *            Function to be applied to each completablefuture on completion
	 * @return This phase in Stream
	 */
	default EagerFutureStream<U> doOnEach(final Function<U, U> fn) {
		return (EagerFutureStream) FutureStream.super.doOnEach(fn);
	}

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
	default <R> EagerFutureStream<R> then(final Function<U, R> fn) {
		return (EagerFutureStream) FutureStream.super.then(fn);
	}

	/*
	 * Merge two simple-react Streams, by merging the Stream of underlying
	 * futures - not suitable for merging infinite Streams - use   
	 * see LazyFutureStream#switchOnNext for infinite Streams
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	EagerFutureStream.of(1,2,3)
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
	default FutureStream<U> merge(SimpleReactStream<U>... streams) {
		return (FutureStream)FutureStream.super.merge(streams);		
	}
	/**
	 * Merges this stream and the supplied Streams into a single Stream where the next value
	 * is the next returned across any of the involved Streams
	 * 
	 * <pre>
	 * {@code
	 * 	LazyFutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
	 * 	EagerFutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
	 * 
	 *  LazyFutureStream<Integer> merged = fast.switchOnNext(slow);  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..] 
	 * }
	 * 
	 * 
	 * @param streams
	 * @return
	 */
	default <R> EagerFutureStream<R> switchOnNext(FutureStream<?>... streams){
		return (EagerFutureStream)merge((SimpleReactStream[])streams);
	}
	/*
	 * Define failure handling for this stage in a stream. Recovery function
	 * will be called after an excption Will be passed a
	 * SimpleReactFailedStageException which contains both the cause, and the
	 * input value.
	 * 
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(1, "a", 2, "b", 3)
	    			.cast(Integer.class)
	    			.onFail(e -> -1)
	    			.toList();
	 * 
	 * }
	 * </pre>
	 * 
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
	default EagerFutureStream<U> onFail(
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (EagerFutureStream) FutureStream.super.onFail(fn);
	}

	/*
	 * Handle failure for a particular class of exceptions only
	 * 
	 * 
	 * <pre>
	 * {@code 
	 * new EagerReact().react(()->1,()->2)
			.then(this::throwException)
			.onFail(IOException.class, this::recoverIO)
			.onFail(RuntimeException.class, this::recoverGeneral)
			.toList();
	 * }
	 * </pre>
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
	default EagerFutureStream<U> onFail(
			Class<? extends Throwable> exceptionClass,
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (EagerFutureStream) FutureStream.super
				.onFail(exceptionClass, fn);
	}

	/*
	 * Capture non-recoverable exception
	 * 
	 * <pre>
	 * {@code 
	 *   EagerFutureStream.of(1, "a", 2, "b", 3, null)
	 *   				  .capture(e -> logger.error(e))
						  .cast(Serializable.class)
						  .toList()
	 * 
	 * }
	 * 
	 * </pre>
	 * @param errorHandler Consumer that captures the exception
	 * 
	 * @return Next stage in stream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#capture(java.util.function
	 * .Consumer)
	 */
	@Override
	default EagerFutureStream<U> capture(
			final Consumer<? extends Throwable> errorHandler) {
		return (EagerFutureStream) FutureStream.super.capture(errorHandler);
	}

	/*
	 * 
	 * <pre>
	 * {@code
	 *  EagerFutureStream.sequentialCommonBuilder().react(()->1,()->2,()->3)
		 									 .map(it->it+100)
		 									 .allOf(c-> HashTreePMap.singleton("numbers",c))
		 									 .block();
	 * 
	 * }
	 * </pre>
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#allOf(java.util.function
	 * .Function)
	 */
	@Override
	default <T, R> EagerFutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (EagerFutureStream) FutureStream.super.allOf(fn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function
	 * .Consumer)
	 */
	@Override
	default EagerFutureStream<U> peek(final Consumer<? super U> consumer) {
		return (EagerFutureStream) FutureStream.super.peek(consumer);
	}

	/*
	 * <pre>
	 * {@code 
	 * List<String> result = new EagerReact()
								.<Integer> react(() -> 1, () -> 2, () -> 3)
								.then(it -> "*" + it)
								.filter(it -> it.startsWith("*"))
								.block();
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function
	 * .Predicate)
	 */
	default EagerFutureStream<U> filter(final Predicate<? super U> p) {
		return (EagerFutureStream) FutureStream.super.filter(p);
	}

	/*
	 * <pre>
	 * {@code
	 * 
	 * List<String> titles = new EagerReact().fromStream(Stream.of(query("Hello, world!")))
				
								.flatMap(Collection::stream)
								.peek(System.out::println)
								.<String>then(url -> getTitle(url))
								.filter(Objects::nonNull)
								.filter(Predicates.take(5))
								.peek(title -> saveTitle(title) )
								.peek(System.out::println)
								.block();
	 * }
	 * </pre>
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#flatMap(java.util.function
	 * .Function)
	 */
	@Override
	default <R> EagerFutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		return (EagerFutureStream) FutureStream.super.flatMap(flatFn);
	}
	/**
	 * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
	 * If in async mode this operation is performed asyncrhonously
	 * If in sync mode this operation is performed synchronously
	 * 
	 * <pre>
	 * {@code 
	 * assertThat( new EagerReact()
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
	default <R> EagerFutureStream<R> flatMapCompletableFuture(
			Function<U, CompletableFuture<R>> flatFn) {
		return (EagerFutureStream) FutureStream.super.flatMapCompletableFuture(flatFn);
	}
	/**
	 * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
	 * This operation is performed synchronously
	 * 
	 * <pre>
	 * {@code 
	 * assertThat( new EagerReact()
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
	default <R> EagerFutureStream<R> flatMapCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn) {
		
		return (EagerFutureStream) FutureStream.super.flatMapCompletableFutureSync(flatFn);
	}


	/*
	 * <pre>
	 * {@code
	 * 		List<String> results = LazyFutureStream.sequentialCommonBuilder()
												   .withRetrier(retrier)
													.react(() -> "new event1", () -> "new event2")
													.retry(this::unreliable)
													.onFail(e -> "default")
													.peek(System.out::println)
													.capture(Throwable::printStackTrace)
													.block();
	 * 
	 * }
	 * </pre>
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#retry(java.util.function
	 * .Function)
	 */
	@Override
	default <R> EagerFutureStream<R> retry(Function<U, R> fn) {

		return (EagerFutureStream) FutureStream.super.retry(fn);
	}

	/*
	 * <pre>
	 * {@code 
	 * Set<Integer> result = new EagerReact()
								.<Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
								.then( it -> it*100)
								.allOf(Collectors.toSet(), it -> it.size())
								.first();
			
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#allOf(java.util.stream.Collector
	 * , java.util.function.Function)
	 */
	@Override
	default <T, R> EagerFutureStream<R> allOf(Collector collector,
			Function<T, R> fn) {

		return (EagerFutureStream) FutureStream.super.allOf(collector, fn);
	}

	/* 
	 * <pre>
	 * {@code 
	 * 				new EagerReact().react(() -> 1)
	 * 								.then(this::load)
									.anyOf(this::process)
									.first();
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 *	
	 * @see com.aol.simple.react.stream.traits.FutureStream#anyOf(java.util.function.Function)
	 */
	default <R> EagerFutureStream<R> anyOf(Function<U, R> fn) {

		return (EagerFutureStream) FutureStream.super.anyOf(fn);
	}

	EagerFutureStream<U> withLastActive(StreamWrapper streamWrapper);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#fromStream(java.util.stream
	 * .Stream)
	 */
	@Override
	default <R> EagerFutureStream<R> fromStream(Stream<R> stream) {
		return (EagerFutureStream) FutureStream.super.fromStream(stream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.simple.react.stream.FutureStreamImpl#fromStreamCompletableFuture
	 * (java.util.stream.Stream)
	 */
	@Override
	default <R> EagerFutureStream<R> fromStreamOfFutures(
			Stream<CompletableFuture<R>> stream) {

		return (EagerFutureStream) FutureStream.super
				.fromStreamOfFutures(stream);
	}

	/*
	 * Take the first (maxSize) completed results from this stage of the Stream
	 * as input to the next stage. e.g.
	 * 
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(()>loadSlow(),()>loadMedium(),()>loadFast())
	 * 				.limit(2)
	 * }
	 *  //[loadFast, loadMedium]
	 * </pre>
	 * will take the results from loadMedium and loadFast()
	 * 
	 * 
	 * @param maxSize The size of the subsequent Stream
	 * 
	 * @return EagerFutureStream
	 * 
	 * @see org.jooq.lambda.Seq#limit(long)
	 */
	@Override
	default EagerFutureStream<U> limit(long maxSize) {
		return fromStream(toQueue().stream().limit(maxSize));
	}

	/**
	 * Perform a limit operation on the underlying Stream of Futures
	 * In contrast to EagerFutureStream#limit this removes entries basaed on their
	 * start position
	 * 
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(()>loadSlow(),()>loadMedium(),()>loadFast())
	 * 				.limitFutures(2)
	 * }
	 * 
	 * //[loadSlow, loadMedium]
	 * </pre>
	 * 
	 * 
	 * 
	 * @param maxSize The size of the subsequent Stream
	 * @return limited Stream
	 */
	default EagerFutureStream<U> limitFutures(long maxSize) {

		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withList(lastActive.stream()
				.limit(maxSize).collect(Collectors.toList()));
		return this.withLastActive(limited);

	}
	/*
	 * Skip the first (n) completed results from this stage of the Stream
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(()>loadSlow(),()>loadMedium(),()>loadFast())
	 * 				.skip(2)
	 * }
	 * 
	 * //[loadSlow]
	 * </pre>
	 * 
	 * will take the results from loadSlow()
	 * 
	 * @param n number of Elements to skip
	 * 
	 * @return EagerFutureStream
	 * 
	 * @see org.jooq.lambda.Seq#skip(long)
	 */
	@Override
	default EagerFutureStream<U> skip(long n) {
		return fromStream(toQueue().stream().skip(n));
	}

	/**
	 * In contast to EagerFutureStream#skip skipFutures will skip the first n entries
	 * of the underlying Stream of Futures.
     * <pre>
	 * {@code 
	 * EagerFutureStream.of(()>loadSlow(),()>loadMedium(),()>loadFast())
	 * 				.skip(2)
	 * }
	 * 
	 * //[loadFast]
	 * </pre>	 


	 * 
	 * @param n
	 * @return
	 */
	default EagerFutureStream<U> skipFutures(long n) {
		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withList(lastActive.stream().skip(n)
				.collect(Collectors.toList()));
		return this.withLastActive(limited);
	}

	/*
	 * Cast all elements in this stream to specified type. May throw {@link
	 * ClassCastException}.
	 * 
	 * <pre>
	 * EagerFutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * </pre>
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
	default <U> EagerFutureStream<U> cast(Class<U> type) {
		return (EagerFutureStream<U>) FutureStream.super.cast(type);
	}

	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * <pre>
	 * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
	 * 
	 * gives a Stream of (1,2,3)
	 * 
	 * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
	 * 
	 * gives a Stream of ("a","b")
	 * </pre>
	 * @see com.aol.simple.react.stream.traits.FutureStream#ofType(java.lang.Class)
	 */
	@Override
	default <U> EagerFutureStream<U> ofType(Class<U> type) {
		return (EagerFutureStream<U>) FutureStream.super.ofType(type);
	}

	

	/**
	 * Concatenate two streams.
	 * 
	 * <pre>
	 * {@code 
	 * // (1, 2, 3, 4, 5, 6) 
	 *  EagerFutureStream.of(1, 2,3).concat(Stream.of(4, 5, 6))
	 * 
	 * 
	 * }
	 * </pre>
	 *
	 * @see #concat(Stream[])
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	default EagerFutureStream<U> concat(Stream<U> other) {
		if (other instanceof SimpleReactStream)
			return (EagerFutureStream) merge((SimpleReactStream) other);
		return fromStream(FutureStream.super.concat(other));
	}
	

	/**
	 * Concatenate two streams.
	 * 
	 * <pre> {@code 
	 * // (1, 2, 3, 4) EagerFutureStream.of(1, 2, 3).concat(4)
	 * }
	 *</pre>
	 * @see #concat(Stream[])
	 */
	default EagerFutureStream<U> concat(U other) {
		return fromStream(FutureStream.super.concat(other));
	}

	/**
	 * Concatenate two streams.
	 * 
	 * <pre>
	 * {@code 
	 * // (1, 2, 3, 4, 5, 6) EagerFutureStream.of(1, 2, 3).concat(4, 5, 6)
	 * }
	 *</pre>
	 * @see #concat(Stream[])
	 */
	@SuppressWarnings({ "unchecked" })
	default EagerFutureStream<U> concat(U... other) {
		return (EagerFutureStream<U>) concat( EagerFutureStream
				.of(other));
	}

	
	 
	/**
	 * Returns a limited interval from a given Stream.
	 * 
	 * <pre>
	 * {@code 
	 * // (4, 5) EagerFutureStream.of(1, 2, 3, 4, 5, 6).sliceFutures(3, 5)
	 * }
	 *</pre>
	 * @see #slice(long, long)
	 */

	default EagerFutureStream<U> sliceFutures(long from, long to) {
		List noType = Seq.seq(getLastActive().stream()).slice(from, to)
				.collect(Collectors.toList());
		return fromListCompletableFuture(noType);
	}

	/**
	 * Returns a limited interval from a given Stream.
	 * 
	 * <pre>
	 * {@code 
	 * // (4, 5) EagerFutureStream.of(1, 2, 3, 4, 5, 6).slice(3, 5)
	 * 
	 * }</pre>
	 *
	 * @see #slice(Stream, long, long)
	 */

	@Override
	default EagerFutureStream<U> slice(long from, long to) {

		return fromStream(FutureStream.super.slice(from, to));
	}

	/**
	 * Zip two streams into one.
	 * <pre>
	 * {@code 
	 * 
	 * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c")) EagerFutureStream.of(1,
	 * 2, 3).zip(EagerFutureStream.of("a", "b", "c"))
	 * 
	 *}</pre>
	 * @see #zip(Stream, Stream)
	 */
	@Override
	default <R> EagerFutureStream<Tuple2<U, R>> zip(Seq<R> other) {
		return fromStream(FutureStream.super.zip(other));
	}

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting
	 * values.
	 * <pre>
	 * <code>
	 * ("1:a", "2:b", "3:c") 
	 * 
	 * EagerFutureStream.of(1, 2,3).zip(EagerFutureStream.of("a", "b", "c"), (i, s) &gt; i + ":" + s)
	 * </code>
	 * </pre>
	 *
	 * @see #zip(Seq, BiFunction)
	 */
	default <T, R> EagerFutureStream<R> zip(Seq<T> other,
			BiFunction<U, T, R> zipper) {
		// non-blocking via Queue
		return fromStream(FutureStream.super.zip(other, zipper));
	}

	/**
	 * Zip two Streams, zipping against the underlying futures of this stream
	 * 
	 * @param other
	 * @return
	 */
	default <R> EagerFutureStream<Tuple2<U,R>> zipFutures(Stream<R> other) {
		return (EagerFutureStream<Tuple2<U,R>>)FutureStream.super.zipFutures(other);

	}
	/**
	 * Zip two Streams, zipping against the underlying futures of both Streams
	 * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
	 * will be populated asyncrhonously
	 * 
	 * @param other  Another FutureStream to zip Futures with
	 * @return New Sequence of CompletableFutures
	 */
	default <R> EagerFutureStream<Tuple2<U,R>> zipFutures(FutureStream<R> other) {
		return (EagerFutureStream<Tuple2<U,R>>)FutureStream.super.zipFutures(other);

	}

	/**
	 * Zip this Stream with an index, but Zip based on the underlying tasks, not completed results.
	 * 
	 * e.g.
	 * two functions that return method name, but take varying lengths of time.
	 * <pre>
	 * <code>
	 * EagerFutureStream.react(()-&gt;takesALotOfTime(),()-&gt;veryQuick()).zipWithIndex();
	 * 
	 *  [["takesALotOfTime",0],["veryQuick",1]]
	 *  
	 *  Where as with standard zipWithIndex you would get a new Stream ordered by completion
	 *  
	 *  [["veryQuick",0],["takesALotOfTime",1]]
	 * </code>
	 * </pre>
	 * @see #zipWithIndex(Stream)
	 */
	default EagerFutureStream<Tuple2<U,Long>> zipFuturesWithIndex() {

		Seq seq = Seq.seq(getLastActive().stream().iterator()).zipWithIndex();
		Seq<Tuple2<CompletableFuture<U>,Long>> withType = (Seq<Tuple2<CompletableFuture<U>,Long>>)seq;
		Stream futureStream =  fromStreamOfFutures((Stream)withType.map(t -> t.v1.thenApply(v -> 
							Tuple.tuple(t.v1.join(),t.v2))));
		return (EagerFutureStream<Tuple2<U,Long>>)futureStream;
		
	}

	/**
	 * Zip a Stream with a corresponding Stream of indexes.
	 * 
	 * <pre>
	 * {@code 
	 * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
	 * EagerFutureStream.of("a", "b", "c").zipWithIndex()
	 * 
	 *}</pre>
	 * @see #zipWithIndex(Stream)
	 */
	default EagerFutureStream<Tuple2<U, Long>> zipWithIndex() {
		return fromStream(FutureStream.super.zipWithIndex());
	}

	/**
	 * Scan a stream to the left.
	 * <pre>
	 * {@code 
	 * 
	 * // ("", "a", "ab", "abc") EagerFutureStream.of("a", "b",
	 * "c").scanLeft("", (u, t) &gt; u + t)
	 * }</pre>
	 * 
	 */
	@Override
	default <T> EagerFutureStream<T> scanLeft(T seed,
			BiFunction<T, ? super U, T> function) {
		return fromStream(FutureStream.super.scanLeft(seed, function));
	}

	/**
	 * Scan a stream to the right.
	 * 
	 * <pre>
	 * {@code 
	 * // ("", "c", "cb", "cba") EagerFutureStream.of("a", "b",
	 * "c").scanRight("", (t, u) &gt; u + t)
	 * }</pre>
	 */
	@Override
	default <R> EagerFutureStream<R> scanRight(R seed,
			BiFunction<? super U, R, R> function) {
		 Seq<R> stream = FutureStream.super.scanRight(seed, function);
		 if(stream instanceof FutureStream)
			 return (EagerFutureStream<R>)stream;
		return fromStream(stream);
	}

	/**
	 * Reverse a stream.
	 * <pre>
	 * {@code 
	 * 
	 * // (3, 2, 1) EagerFutureStream.of(1, 2, 3).reverse()
	 * }</pre>
	 */
	@Override
	default EagerFutureStream<U> reverse() {
		return fromStream(FutureStream.super.reverse());
	}
	/**
	 * Reversed, operating on the underlying futures.
	 * <pre>
	 * {@code 
	 * 
	 * // (3, 2, 1) EagerFutureStream.of(1, 2, 3).reverse()
	 * }</pre>
	 * 
	 * @return
	 */
	default EagerFutureStream<U> reverseFutures() {
		StreamWrapper lastActive = getLastActive();
		ListIterator<CompletableFuture> it = lastActive.list().listIterator();
		List<CompletableFuture> result = new ArrayList<>();
		while(it.hasPrevious())
			result.add(it.previous());
		
		StreamWrapper limited = lastActive.withList( result);
		return this.withLastActive(limited);
		
	}

	
	
	/**
	 * Shuffle a stream
	 * <pre>
	 * {@code 
	 * 
	 * // e.g. (2, 3, 1) EagerFutureStream.of(1, 2, 3).shuffle()
	 * 
	 * }</pre>
	 * 
	 */
	@Override
	default EagerFutureStream<U> shuffle() {
		return fromStream(FutureStream.super.shuffle());
	}

	/**
	 * Shuffle a stream using specified source of randomness
	 * <pre>
	 * {@code 
	 * 
	 * // e.g. (2, 3, 1) EagerFutureStream.of(1, 2, 3).shuffle(new Random())
	 * 
	 * }</pre>
	 */
	@Override
	default EagerFutureStream<U> shuffle(Random random) {
		return fromStream(FutureStream.super.shuffle(random));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to true.
	 * <pre>
	 * {@code 
	 * 
	 * // (3, 4, 5) EagerFutureStream.of(1, 2, 3, 4, 5).skipWhile(i &gt; i &lt;
	 * 3)
	 * 
	 *}</pre>
	 * @see #skipWhile(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> skipWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipWhile(predicate));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to false.
	 * <pre>
	 * {@code 
	 * 
	 * // (3, 4, 5) EagerFutureStream.of(1, 2, 3, 4, 5).skipUntil(i &gt; i == 3)
	 * 
	 *}</pre>
	 * @see #skipUntil(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> skipUntil(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipUntil(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to true.
	 * <pre>
	 * {@code 
	 * 
	 * // (1, 2) EagerFutureStream.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt;
	 * 3)
	 * 
	 * }</pre>
	 * @see #limitWhile(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> limitWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.limitWhile(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to false.
	 * <pre>
	 * {@code 
	 * 
	 * // (1, 2) EagerFutureStream.of(1, 2, 3, 4, 5).limitUntil(i &gt; i == 3)
	 * 
	 *}</pre>
	 * @see #limitUntil(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> limitUntil(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.limitUntil(predicate));
	}

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * <pre>
	 * {@code 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) EagerFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * 
	 *}</pre>
	 * @see #intersperse(Stream, Object)
	 */
	@Override
	default EagerFutureStream<U> intersperse(U value) {
		return fromStream(FutureStream.super.intersperse(value));
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#distinct()
	 */
	@Override
	default EagerFutureStream<U> distinct() {
		return fromStream(toQueue().stream().distinct());
	}

	/**
	 * Duplicate a Stream into two equivalent LazyFutureStreams, underlying
	 * Stream of Futures is duplicated
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(1, 2, 3).duplicate()
	 * 
	 * results in
	 * 
	 * tuple((1,2,3),(1,2,3))
	 * }</pre>
	 * Care should be taken not to use this method with infinite streams!
	 * 
	 * @return Two equivalent Streams
	 * 
	 * @see #duplicate()
	 */

	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> duplicateFutures() {
		// unblocking impl
		Stream stream = getLastActive().stream();
		Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> duplicated = Seq
				.seq((Stream<CompletableFuture<U>>) stream).duplicate();
		Tuple2 dup =new Tuple2(fromStreamOfFutures(duplicated.v1),
				fromStreamOfFutures(duplicated.v2));
	
		return (Tuple2<EagerFutureStream<U>,EagerFutureStream<U>>) dup;
	}
	/**
	 * Duplicate a Stream into two equivalent LazyFutureStreams, Streams are duplicated as results
	 * of Futures are populated
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(1, 2, 3).duplicate()
	 * 
	 * results in
	 * 
	 * tuple((1,2,3),(1,2,3))
	 * }</pre>
	 * Care should be taken not to use this method with infinite streams!
	 * 
	 * @return Two equivalent Streams
	 * 
	 * @see #duplicate()
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> duplicate() {

		Tuple2<Seq<U>, Seq<U>> duplicated = FutureStream.super.duplicate();
		return new Tuple2(fromStream(duplicated.v1), fromStream(duplicated.v2));
	}

	

	

	/**
	 * Partition a stream into two given a predicate. (Operates on results, not
	 * futures)
	 * Can't change the return type in Seq to incorporate EagerFutureStream - see partitionFutureStream instead.
	 * 
	 * <pre>
	 * {@code 
	 * // tuple((1, 3, 5), (2, 4, 6)) 
	 * EagerFutureStream.of(1, 2, 3, 4, 5,6).partition(i -> i % 2 != 0)
	 * }</pre>
	 *
	 * @see #partition(Stream, Predicate)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> partition(Predicate<? super U> predicate) {
		Tuple2<Seq<U>, Seq<U>> partitioned = FutureStream.super
				.partition(predicate);
		return new Tuple2(fromStream(partitioned.v1),
				fromStream(partitioned.v2));
	}

	/**
	 * Partition an EagerFutureStream into two EagerFutureStreams given a
	 * predicate.
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
	 * 
	 * results in
	 * 
	 * tuple((1, 3, 5), (2, 4, 6))
	 * }</pre>
	 * @param predicate
	 *            Predicate to split Stream
	 * @return EagerFutureStream
	 * @see #partition(Predicate)
	 */
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> partitionFutureStream(
			Predicate<? super U> predicate) {
		Tuple2 partition = partition(predicate);
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) partition;
	}

	/**
	 * Split a stream at a given position. (Operates on futures)
	 * <pre>
	 * {@code  
	 * // tuple((1, 2, 3), (4, 5, 6))
	 *  EagerFutureStream.of(1, 2, 3, 4, 5,6).splitAt(3)
	 *  
	 *  }</pre>
	 * @return 
	 * 
	 *
	 * @see #splitAt(Stream, long)
	 */
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> splitAtFutures(
			long position) {
		Stream stream = getLastActive().stream();
		Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> split = Seq
				.seq((Stream<CompletableFuture<U>>) stream).splitAt(position);

		return new Tuple2(
				fromListCompletableFuture(split.v1.collect(Collectors.toList())),
				fromListCompletableFuture(split.v2.collect(Collectors.toList())));
	}

	/**
	 * Split a EagerFutureStream at a given position.
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitAt(3)
	 * 
	 * results in tuple((1, 2, 3), (4, 5, 6))
	 * }</pre>
	 * 
	 * @see #splitAt(long)
	 */
	default Tuple2<Seq<U>, Seq<U>> splitAt(long position) {
		// blocking impl

		Tuple2<Seq<U>, Seq<U>> split = FutureStream.super.splitAt(position);
		return new Tuple2(fromStream(split.v1), fromStream(split.v2));
	}
	
	/**
	 * Split a EagerFutureStream at a given position. Return type is an EagerFutureStream
	 * <pre>
	 * {@code 
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitAtFutureStream(3)
	 * 
	 * results in tuple((1, 2, 3), (4, 5, 6))
	 * }</pre>
	 * 
	 * @see #splitAt(long)
	 */
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> splitAtFutureStream(
			long position) {
		Tuple2 split = splitAt(position);
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) split;
	}

	/* 
	 * Convert a List of CompletableFutures into an EagerFutureStream
	 * 
	 *	@param list List of CompletableFutures
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#fromListCompletableFuture(java.util.List)
	 */
	default <R> EagerFutureStream<R> fromListCompletableFuture(
			List<CompletableFuture<R>> list) {

		return (EagerFutureStream) FutureStream.super
				.fromListCompletableFuture(list);
	}

	/**
	 * Split a stream at the head.
	 * 
	 * <pre>
	 * {@code 
	 * // tuple(1, (2, 3, 4, 5, 6)) 
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitHead(3)
	 * }</pre>
	 *
	 * @see #splitAt(Stream, long)
	 */
	@Override
	default Tuple2<Optional<U>, Seq<U>> splitAtHead() {
		// blocking
		Tuple2<Optional<U>, Seq<U>> split = FutureStream.super.splitAtHead();
		return new Tuple2(split.v1, fromStream(split.v2));
	}

	/**
	 * SplitAtHead but return type is EagerFutureStream
	 * 
	 * @return
	 * @see #splitAtHead()
	 */
	default Tuple2<Optional<U>, EagerFutureStream<U>> splitAtHeadFutureStream() {
		Tuple2 split = splitAtHead();
		return split;
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
	@Override
	default EagerFutureStream<U> parallel(){
		return this.withAsync(true).withTaskExecutor(EagerReact.parallelBuilder().getExecutor());
	}
	/* 
	 *	@return  New version of this stream  converted to execute synchronously and sequentially
	 * @see com.aol.simple.react.stream.traits.FutureStream#sequential()
	 */
	@Override
	default EagerFutureStream<U> sequential(){
		return this.withAsync(false).withTaskExecutor(EagerReact.sequentialBuilder().getExecutor());
	}




	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#unordered()
	 */
	@Override
	default EagerFutureStream<U> unordered() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
	 */
	@Override
	default EagerFutureStream<U> onClose(Runnable closeHandler) {

		return (EagerFutureStream)FutureStream.super.onClose(closeHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted()
	 */
	@Override
	default EagerFutureStream<U> sorted() {
		return (EagerFutureStream<U>)fromStream(FutureStream.super.sorted());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	default EagerFutureStream<U> sorted(Comparator<? super U> comparator) {
		return (EagerFutureStream<U>)fromStream(FutureStream.super.sorted(comparator));
	}

	/**
	 * Give a function access to the current stage of a SimpleReact Stream
	 * 
	 * @param consumer
	 *            Consumer that will recieve current stage
	 * @return Self (current stage)
	 */
	default EagerFutureStream<U> self(Consumer<FutureStream<U>> consumer) {
		return (com.aol.simple.react.stream.traits.EagerFutureStream<U>)FutureStream.super.self(consumer);
	}
	
	/**
	 * Merge two reactive dataflows with one and another.
	 * 
	 * @param s1
	 *            Reactive stage builder to merge
	 * @param s2
	 *            Reactive stage builder to merge
	 * @return Merged dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> FutureStream<R> merge(EagerFutureStream s1, EagerFutureStream s2) {
		List merged = Stream
				.of(s1.getLastActive().list(), s2.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (FutureStream<R>) s1.withLastActive(new StreamWrapper(merged));
	}


	/**
	 * Construct an EagerFutureStream Stream from specified array, that will run in parallel
	 * on the common Parallel executor service (by default the Common ForkJoinPool) see ThreadPools#setUseCommon 
	 * to change to a different pool
	 * 
	 * @param array
	 *            Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> EagerFutureStream<U> parallel(U... array) {
		return EagerReact.parallelCommonBuilder().of(Arrays.asList(array));
	}

	
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on the supplied CompletableFutures executor service (unless async operator invoked
	 *  , in which it will switch to the common 'free' thread executor)
	 *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
	 *  
	 * @see Stream#of(Object)
	 */
	static <T> EagerFutureStream<T> eagerFutureStreamFrom(Stream<CompletableFuture<T>> stream) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false)
									.fromStream(stream);
	}
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on the supplied CompletableFutures executor service (unless async operator invoked
	 *  , in which it will switch to the common 'free' thread executor)
	 *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
	 *  
	 * @see Stream#of(Object)
	 */
	static <T> EagerFutureStream<T> eagerFutureStream(CompletableFuture<T> value) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false)
									.fromStream(Stream.of(value));
	}
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
	 *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
	 *  is invoked.
	 *  
	 * @see Stream#of(Object)
	 */
	static <T> EagerFutureStream<T> eagerFutureStream(CompletableFuture<T>... values) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false)
									.fromStream(Stream.of(values));
	}

	
	/**
	 *  Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
	 *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
	 *  is invoked.
	 *  
	 * @see Stream#of(Object)
	 */
	static <T> EagerFutureStream<T> react(Supplier<T> value) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false).react(value);
	}

	/**
	 * Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
	 * The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator is invoked.
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> EagerFutureStream<T> react(Supplier<T>... values) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false).react(values);
	}
	/**
	 *  Create a sequential synchronous stream that runs on the current thread
	 * @see Stream#of(Object)
	 * 
	 */
	static <T> EagerFutureStream<T> of(T value) {
		return eagerFutureStream((Stream) Stream.of(value));
	}

	/**
	 * Create a sequential synchronous stream that runs on the current thread
	 * @see Stream#of(Object[])
	 * 
	 */
	@SafeVarargs
	static <T> EagerFutureStream<T> of(T... values) {
		return eagerFutureStream((Stream) Stream.of(values));
	}
	/**
	 *  Create a sequential synchronous stream that runs on the current thread
	 * @see Stream#of(Object)
	 * 
	 */
	static <T> EagerFutureStream<T> ofThread(T value) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false).of(value);
	}

	/**
	 * Create a sequential synchronous stream that runs on the current thread
	 * @see Stream#of(Object[])
	 * 
	 */
	@SafeVarargs
	static <T> EagerFutureStream<T> ofThread(T... values) {
		return  new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false).of(values);
	}

	/**
	 * @see Stream#empty()
	 */
	static <T> EagerFutureStream<T> empty() {
		return eagerFutureStream((Stream) Seq.empty());
	}

	/**
	 * Wrap a Stream into a Sequential synchronous FutureStream that runs on the current thread 
	 */
	static <T> EagerFutureStream<T> eagerFutureStream(Stream<T> stream) {
		if (stream instanceof FutureStream)
			return (EagerFutureStream<T>) stream;
		EagerReact er = new EagerReact(
		ThreadPools.getCurrentThreadExecutor(), RetryBuilder.getDefaultInstance()
		.withScheduler(ThreadPools.getSequentialRetry()),false);
		
		return new EagerFutureStreamImpl<T>(er,
				stream.map(CompletableFuture::completedFuture)).sync();
	}

	/**
	 * Wrap an Iterable into a FutureStream that runs on the current thread
	 * 
	 * 
	 */
	static <T> EagerFutureStream<T> eagerFutureStream(Iterable<T> iterable) {
		return eagerFutureStream(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream that runs on the current thread
	 * 
	 */
	static <T> EagerFutureStream<T> eagerFutureStream(Iterator<T> iterator) {
		return eagerFutureStream(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}


	

}
