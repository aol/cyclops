package com.aol.simple.react.stream.traits;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.stream.CloseableIterator;

public interface FutureStream<U> extends Seq<U> {

	
	
	/**
	 * 
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = LazyFutureStream.of(1,2,3,4,5,6)
				.map(i->i+2)
				.toLazyCollection();
	 * }
	 * </pre>
	 * 
	 * @return a LazyCollection from this Stream
	 * 			Collection will not be populated until methods are accessed.
	 * 			(Eager streams may populate a LazyColleciton in the background however)
	 */
	Collection<U> toLazyCollection();
	/**
	 * Create a LazyCollection access to which is synchronized
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = LazyFutureStream.of(1,2,3,4,5,6)
				.map(i->i+2)
				.toConcurrentLazyCollection();
	 * }
	 * </pre>
	 * 
	 * @return a LazyCollection from this Stream
	 * 			Collection will not be populated until methods are accessed.
	 * 			(Eager streams may populate a LazyColleciton in the background however)
	 */
	Collection<U> toConcurrentLazyCollection();
	/**
	 * <pre>
	 * {@code 
	 * 	 int num  =	LazyFutureStream.of(1).single();
	 * 
	 *  //num is 1
	 * }
	 * </pre>
	 * 
	 * @return single element from this Stream if it is a single element Stream
	 * 			otherwise throw an UnsupportedOperationException
	 */
	U single();
	/**
	 * Zip two Streams, zipping against the underlying futures of this stream
	 * 
	 * @param other
	 * @return
	 */
	<R> FutureStream<Tuple2<U,R>> zipFutures(Stream<R> other);
	/* 
	 * @see org.jooq.lambda.Seq#crossJoin(java.util.stream.Stream)
	 */
	@Override
	 <T> FutureStream<Tuple2<U, T>> crossJoin(Stream<T> other);
	/**
     * Produce this stream, or an alternative stream from the
     * {@code value}, in case this stream is empty.
     */
	@Override
	FutureStream<U> onEmpty(U value);
	/**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	@Override
	FutureStream<U> onEmptyGet(Supplier<U> supplier);
	 /**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	@Override
	<X extends Throwable> FutureStream<U> onEmptyThrow(Supplier<X> supplier);
    
	/* 
	 * @see org.jooq.lambda.Seq#innerJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	@Override
    <T> FutureStream<Tuple2<U, T>> innerJoin(Stream<T> other, BiPredicate<U, T> predicate);

    
    /* 
     * @see org.jooq.lambda.Seq#leftOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
     */
	@Override
     <T> FutureStream<Tuple2<U, T>> leftOuterJoin(Stream<T> other, BiPredicate<U, T> predicate);

	/* 
	 * @see org.jooq.lambda.Seq#rightOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	@Override
    <T> FutureStream<Tuple2<U, T>> rightOuterJoin(Stream<T> other, BiPredicate<U, T> predicate);
   
	
	
	
	
	
	/**
	 * Zip two Streams, zipping against the underlying futures of both Streams
	 * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
	 * will be populated asyncrhonously
	 * 
	 * @param other  Another FutureStream to zip Futures with
	 * @return New Sequence of CompletableFutures
	 */

	<R> FutureStream<Tuple2<U,R>> zipFutures(FutureStream<R> other);
	
	/**
	 * @return an Iterator that chunks all completed elements from this stream since last it.next() call into a collection
	 */
	Iterator<Collection<U>> chunkLastReadIterator();
	/**
	 * @return a Stream that batches all completed elements from this stream since last read attempt into a collection
	 */
	 FutureStream<Collection<U>> chunkSinceLastRead();
	/**
	 * Break a stream into multiple Streams based of some characteristic of the elements of the Stream
	 * 
	 * e.g. 
	 * 
	 * EagerFutureStream.of(10,20,25,30,41,43).shard(ImmutableMap.of("even",new Queue(),"odd",new Queue(),element-&gt; element%2==0? "even" : "odd");
	 * 
	 * results in 2 Streams
	 * "even": 10,20,30
	 * "odd" : 25,41,43
	 * 
	 * @param shards Map of Queue's keyed by shard identifier
	 * @param sharder Function to split split incoming elements into shards
	 * @return Map of new sharded Streams
	 */
	<K> Map<K, ? extends FutureStream<U>> shard(
			Map<K, Queue<U>> shards, Function<U, K> sharder) ;
	
	/**
	 * Cancel the CompletableFutures in this stage of the stream
	 */
	void cancel();
	
	/**
	 * Batch the elements in the Stream by a combination of Size and Time
	 * If batch exceeds max size it will be split
	 * If batch exceeds max time it will be split
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * @return
	 */
	FutureStream<List<U>> batchBySizeAndTime(int size,long time, TimeUnit unit);
	/**
	 * Batch the elements in the Stream by a combination of Size and Time
	 * If batch exceeds max size it will be split
	 * If batch exceeds max time it will be split
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * @return
	 */
	<C extends Collection<U>> FutureStream<C> batchBySizeAndTime(int size,long time, TimeUnit unit, Supplier<C> factory);
	/**
	 * 
	 * Batch the elements in this stream into Lists of specified size
	 * 
	 * @param size Size of lists elements should be batched into
	 * @return Stream of Lists
	 */
	FutureStream<List<U>> batchBySize(int size);
	/**
	 * Batch elements into a Stream of collections with user defined function
	 * @param fn Function takes a supplier, which can be used repeatedly to get the next value from the Stream. If there are no more values, a ClosedQueueException will be thrown.
	 *           This function should return a Supplier which creates a collection of the batched values
	 * @return Stream of batched values
	 */
	<C extends Collection<U>>FutureStream<C> batch(Function<Supplier<U>, Supplier<C>> fn);
	
	/**
	 * Batch the elements in this stream into Collections of specified size
	 * The type of Collection is determined by the specified supplier
	 * 
	 * @param size Size of batch
	 * @param supplier Create the batch holding collection
	 * @return Stream of Collections
	 */
	<C extends Collection<U>>FutureStream<C> batchBySize(int size, Supplier<C> supplier);
	/**
	 * Introduce a random delay between events in a stream
	 * Can be used to prevent behaviour synchronizing within a system
	 * 
	 * @param jitterInNanos Max number of nanos for jitter (random number less than this will be selected)/
	 * @return Next stage in Stream with jitter applied
	 */
	FutureStream<U> jitter(long jitterInNanos);
	/**
	 * Apply a fixed delay before emitting elements to the next phase of the Stream.
	 * Note this doesn't neccessarily imply a fixed delay between element creation (although it may do).
	 * e.g.
	 * 
	 * EagerFutureStream.of(1,2,3,4).fixedDelay(1,TimeUnit.hours);
	 * 
	 * Will emit 1 on start, then 2 after an hour, 3 after 2 hours and so on.
	 * 
	 * However all 4 numbers will be populated in the Stream immediately.
	 * 
	 * LazyFutureStream.of(1,2,3,4).withQueueFactories(QueueFactories.boundedQueue(1)).fixedDelay(1,TimeUnit.hours);
	 * 
	 * Will populate each number in the Stream an hour apart.
	 * 
	 * @param time amount of time between emissions
	 * @param unit TimeUnit for emissions
	 * @return Next Stage of the Stream
	 */
	FutureStream<U> fixedDelay(long time, TimeUnit unit) ;
	/**
	 * Allows clients to control the emission of data for the next phase of the Stream.
	 * The user specified function can delay, drop, or change elements
	 * 
	 * @param fn Function takes a supplier, which can be used repeatedly to get the next value from the Stream. If there are no more values, a ClosedQueueException will be thrown.
	 *           This function should return a Supplier which returns the desired result for the next element (or just the next element).
	 * @return Next stage in Stream
	 */
	FutureStream<U> control(Function<Supplier<U>, Supplier<U>> fn);
	
	/**
	 * Can be used to debounce (accept a single data point from a unit of time) data.
	 * This drops data. For a method that slows emissions and keeps data #see#onePer
	 * 
	 * @param time Time from which to accept only one element
	 * @param unit Time unit for specified time
	 * @return Next stage of stream, with only 1 element per specified time windows
	 */
	FutureStream<U> debounce(long time, TimeUnit unit);
	/**
	 * Slow emissions down, emiting one element per specified time period
	 * 
	 * @param time Frequency period of element emission
	 * @param unit Time unit for frequency period
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	FutureStream<U> onePer(long time, TimeUnit unit) ;
	/**
	 * Allows x (specified number of) emissions with a time period before stopping emmissions until specified time has elapsed since last emission
	 * 
	 * @param x Number of allowable emissions per time period
	 * @param time Frequency time period
	 * @param unit Frequency time unit
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	FutureStream<U> xPer(int x,long time, TimeUnit unit);
	/**
	 * Organise elements in a Stream into a Collections based on the time period they pass through this stage
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * @param time Time period during which all elements should be collected
	 * @param unit Time unit during which all elements should be collected
	 * @return Stream of Lists
	 */
	FutureStream<Collection<U>> batchByTime(long time, TimeUnit unit);
	/**
	 * Organise elements in a Stream into a Collections based on the time period they pass through this stage
	 * 
	 * @param time Time period during which all elements should be collected
	 * @param unit Time unit during which all elements should be collected
	 * @param factory Instantiates the collections used in the batching
	 * @return Stream of collections
	 */
	FutureStream<Collection<U>> batchByTime(long time, TimeUnit unit,Supplier<Collection<U>> factory);

	/**
	 * 
	 * Similar to zip and combineLatest, except will always take the latest from this Stream while taking the last available value from the provided stream.
	 * By contrast zip takes new / latest values from both Streams and combineLatest takes the latest from either Stream (merged with last available from the other).
	 * 
	 * @param s Stream to merge with
	 * @return Stream of Tuples with the latest values from this stream
	 */
	<T> FutureStream<Tuple2<U, T>> withLatest(FutureStream<T> s);
	/**
	 * Similar to zip and withLatest, except will always take the latest from either Stream (merged with last available from the other).
	 * By contrast zip takes new / latest values from both Streams and withLatest will always take the latest from this Stream while 
	 * taking the last available value from the provided stream.
	 * 
	 * @param s Stream to merge with
	 * @return  Stream of Tuples with the latest values from either stream
	 */
	<T> FutureStream<Tuple2<U, T>> combineLatest(FutureStream<T> s);
	/**
	 * Return a Stream with the same values as this Stream, but with all values omitted until the provided stream starts emitting values.
	 * Provided Stream ends the stream of values from this stream.
	 * 
	 * @param s Stream that will start the emission of values from this stream
	 * @return Next stage in the Stream but with all values skipped until the provided Stream starts emitting
	 */
	<T>  FutureStream<U> skipUntil(FutureStream<T> s);
	/**
	 * Return a Stream with the same values, but will stop emitting values once the provided Stream starts to emit values.
	 * e.g. if the provided Stream is asynchronously refreshing state from some remote store, this stream can proceed until
	 * the provided Stream succeeds in retrieving data.
	 * 
	 * @param s Stream that will stop the emission of values from this stream
	 * @return Next stage in the Stream but will only emit values until provided Stream starts emitting values
	 */
	<T>  FutureStream<U> takeUntil(FutureStream<T> s);

	
	
	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#retry(java.util.
	 * function.Function)
	 */
	<R> FutureStream<R> retry(final Function<U, R> fn);

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#allOf(java.util.
	 * stream.Collector, java.util.function.Function)
	
	default <T, R> FutureStream<R> allOf(final Collector collector,
			final Function<T, R> fn) {
		return (FutureStream) SimpleReactStream.super.allOf(collector, fn);
	}

	default <R> FutureStream<R> anyOf(final Function<U, R> fn) {
		return (FutureStream) SimpleReactStream.super.anyOf(fn);
	}
 */
	/*
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#
	 * fromStreamCompletableFuture(java.util.stream.Stream)
	 */
	 <R> FutureStream<R> fromStreamOfFutures(
			Stream<CompletableFuture<R>> stream);
	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#then(java.util.function
	 * .Function)
	 */
	<R> FutureStream<R> then(final Function<U, R> fn) ;
	

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#fromStream(java.
	 * util.stream.Stream)
	 */
	<R> FutureStream<R> fromStream(Stream<R> stream);
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
	 * }</pre>
	 *
	 * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
	 * 
	 * 
	 * @param flatFn flatMap function
	 * @return Flatten Stream with flatFn applied
	 */
	 <R> FutureStream<R> flatMapCompletableFuture(
			Function<U, CompletableFuture<R>> flatFn) ;
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
	 <R> FutureStream<R> flatMapCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn);

	/*
	 * @see org.jooq.lambda.Seq#flatMap(java.util.function.Function)
	 */
	@Override
	<R> FutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn);
	
	
	
	
	/*
	 * @see org.jooq.lambda.Seq#peek(java.util.function.Consumer)
	 */
	@Override
	FutureStream<U> peek(final Consumer<? super U> consumer);

	/*
	 * @see org.jooq.lambda.Seq#filter(java.util.function.Predicate)
	 */
	FutureStream<U> filter(final Predicate<? super U> p) ;

	/**
	 * Stream supporting methods
	 */

	
	/*
	 * Sequentially iterate through the LazyFutureStream
	 * To run a parallel Stream in parallel use run or run on current
	 *  
	 *@param action Consumer for each element in the Stream
	 * @see org.jooq.lambda.Seq#forEach(java.util.function.Consumer)
	 */
	@Override
	void forEach(Consumer<? super U> action);
	
	/* 
	 * Sequentially iterate through the LazyFutureStream
	 * To run a parallel Stream in parallel use run or run on current
	 * 
	 *	@param action Consumer for each element
	 * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
	 */
	@Override
	void forEachOrdered(Consumer<? super U> action);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#toArray()
	 */
	@Override
	Object[] toArray();
	
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
	FutureStream<List<U>> sliding(int size);
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
	FutureStream<List<U>> sliding(int size, int increment);
	
	/**
	 * @return An extensive set of asyncrhonous terminal operations
	 */
	FutureOps<U> futureOperations();
	/**
	 * @param executor to execute terminal ops asynchronously on
	 * @return An extensive set of asyncrhonous terminal operations
	 */
	FutureOps<U> futureOperations(Executor executor);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
	 */
	@Override
	<A> A[] toArray(IntFunction<A[]> generator);
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BinaryOperator)
	 */
	@Override
	U reduce(U identity, BinaryOperator<U> accumulator);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.util.function.BinaryOperator)
	 */
	@Override
	Optional<U> reduce(BinaryOperator<U> accumulator);
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#collect(java.util.function.Supplier,
	 * java.util.function.BiConsumer, java.util.function.BiConsumer)
	 */
	@Override
	 <R> R collect(Supplier<R> supplier,
			BiConsumer<R, ? super U> accumulator, BiConsumer<R, R> combiner);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	@Override
	Optional<U> min(Comparator<? super U> comparator);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	@Override
	Optional<U> max(Comparator<? super U> comparator);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#count()
	 */
	@Override
	long count() ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
	 */
	@Override
	boolean anyMatch(Predicate<? super U> predicate);
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
	 */
	@Override
	boolean allMatch(Predicate<? super U> predicate) ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#noneMatch(java.util.function.Predicate)
	 */
	@Override
	boolean noneMatch(Predicate<? super U> predicate);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findFirst()
	 */
	@Override
	Optional<U> findFirst() ;
	/**
	 * <pre>
	 * {@code 
	 * 	int first = LazyFutureStream.of(1,2,3,4)
	 * 					.firstValue();
	 *  
	 *   //first is 1
	 * }
	 * </pre>
	 * 
	 * @return the firstValue in this stream - same as findFirst but doesn't 
	 * 				return an Optional. Will throw an exception if empty
	 */
	U firstValue();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findAny()
	 */
	@Override
	Optional<U> findAny();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	@Override
	<R> R reduce(R identity, BiFunction<R, ? super U, R> accumulator,
			BinaryOperator<R> combiner);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.BaseStream#iterator()
	 */
	@Override
	CloseableIterator<U> iterator();
	
 
	
	 /* 
	  * More efficient reverse implementation than Seq version
	  * 
	  * 
	  *	@return
	  * @see org.jooq.lambda.Seq#reverse()
	  */
	FutureStream<U> reverse();
	Iterator<U> reversedIterator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#spliterator()
	 */
	@Override
	Spliterator<U> spliterator();
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.BaseStream#isParallel()
	 */
	@Override
	boolean isParallel();

	/*
	 * Creates a sequential instance by populating an async Queue from the
	 * current stream, and reading sequentially from that Stream.
	 * 
	 * For an alternative approach change the task executors to single thread
	 * model, via withTaskExecutor and withRetrier
	 * 
	 * @return Sequential Stream
	 * 
	 * @see com.aol.simple.react.stream.traits.FutureStream#sequential()
	 */
	@Override
	Seq<U> sequential();



	@Override
	Stream<U> stream();
	/*
	 * Seq supporting methods
	 */

	/**
	 * native Seq
	 * 
	 */

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * 
	 *
	 * @see #intersperse(Stream, Object)
	 */
	FutureStream<U> intersperse(U value);

	/*
	 * 
	 * @param type
	 * 
	 * @return
	 * 
	 * @see org.jooq.lambda.Seq#cast(java.lang.Class)
	 */

	@Override
	<U> FutureStream<U> cast(Class<U> type);

	@Override
	<U> FutureStream<U> ofType(Class<U> type);

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#unordered()
	 */
	@Override
	FutureStream<U> unordered() ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
	 */
	@Override
	 Seq<U> onClose(Runnable closeHandler);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#close()
	 */
	@Override
	 void close() ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#map(java.util.function.Function)
	 */
	@Override
	<R> FutureStream<R> map(Function<? super U, ? extends R> mapper);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#mapToInt(java.util.function.ToIntFunction)
	 */
	@Override
	IntStream mapToInt(ToIntFunction<? super U> mapper);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#mapToLong(java.util.function.ToLongFunction)
	 */
	@Override
	LongStream mapToLong(ToLongFunction<? super U> mapper);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#mapToDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMapToInt(java.util.function.Function)
	 */
	@Override
	IntStream flatMapToInt(
			Function<? super U, ? extends IntStream> mapper) ;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMapToLong(java.util.function.Function)
	 */
	@Override
	LongStream flatMapToLong(
			Function<? super U, ? extends LongStream> mapper);
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMapToDouble(java.util.function.Function)
	 */
	@Override
	DoubleStream flatMapToDouble(
			Function<? super U, ? extends DoubleStream> mapper) ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted()
	 */
	@Override
	Seq<U> sorted();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	Seq<U> sorted(Comparator<? super U> comparator);

	/**
	 * Give a function access to the current stage of a SimpleReact Stream
	 * 
	 * @param consumer
	 *            Consumer that will recieve current stage
	 * @return Self (current stage)
	 */
	FutureStream<U> self(Consumer<FutureStream<U>> consumer);

	/**
	 * Returns a limited interval from a given Stream.
	 * 
	 * 
	 * // (4, 5) Seq.of(1, 2, 3, 4, 5, 6).slice(3, 5)
	 * 
	 *
	 * @see #slice(Stream, long, long)
	 */
	@Override
	Seq<U> slice(long from, long to) ;

	

	

	
	/* 
	 * Execute subsequent stages on the completing thread (until async called)
	 * 10X faster than async execution.
	 * Use async for blocking IO or distributing work across threads or cores.
	 * Switch to sync for non-blocking tasks when desired thread utlisation reached
	 * 
	 *	@return Version of FutureStream that will use sync CompletableFuture methods
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#sync()
	 */
	FutureStream<U> sync();
	

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
	FutureStream<U> async();
	Queue<U> toQueue();
	Continueable getSubscription();
	
}