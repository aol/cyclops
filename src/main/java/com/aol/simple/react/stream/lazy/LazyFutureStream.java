package com.aol.simple.react.stream.lazy;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyToQueue;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */
public interface LazyFutureStream<U> extends FutureStream<U>, LazyToQueue<U> {

	LazyFutureStream<U> withLastActive(StreamWrapper streamWrapper);
	
	
	
	 
	/* 
	 * Cast all elements in this stream to specified type. May throw {@link ClassCastException}.
	 * 
	 *  LazyFutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
	 *  
	 *  will throw a ClassCastException
	 *  
	 *	@param type Type to cast to
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.traits.FutureStream#cast(java.lang.Class)
	 */
	@Override
	default <U> LazyFutureStream<U> cast(Class<U> type) {
		return (LazyFutureStream<U>) FutureStream.super.cast(type);
	}
	
	 /**
     * Keep only those elements in a stream that are of a given type.
     * <p>
     * <code><pre>
     * 
     * LazyFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
     * 
     * gives a Stream of (1,2,3)
     * 
     * LazyFutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
     * 
     * gives a Stream of ("a","b")
     * </pre></code>
     *  @see com.aol.simple.react.stream.traits.FutureStream#ofType(java.lang.Class)
     */
	@Override
	default <U> FutureStream<U> ofType(Class<U> type){
		return (LazyFutureStream<U>)FutureStream.super.ofType(type);
	}
	 
	 /**
     * Returns a stream with a given value interspersed between any two values of this stream.
     * <p>
     * <code><pre>
     * // (1, 0, 2, 0, 3, 0, 4)
     * LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
     * </pre></code>
     *
     * @see #intersperse(Stream, Object)
     */
	@Override
	 default LazyFutureStream<U> intersperse(U value) {
	        return (LazyFutureStream<U>)FutureStream.super.intersperse(value);
	 }
		
	
	

	/* 
	 * 
	 * LazyFutureStream.of(1,2,3,4).limit(2)
	 * 
	 * Will result in a Stream of (1,2). Only the first two elements are used.
	 * 
	 *	@param maxSize number of elements to take
	 *	@return Limited LazyFutureStream
	 * @see org.jooq.lambda.Seq#limit(long)
	 */
	@Override
	default LazyFutureStream<U> limit(long maxSize) {

		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withStream(lastActive.stream().limit(maxSize));
		return this.withLastActive(limited);

	}

	/* 
	 * LazyFutureStream.of(1,2,3,4).skip(2)
	 * 
	 * Will result in a stream of (3,4). The first two elements are skipped.
	 * 
	 *	@param n  Number of elements to skip
	 *	@return LazyFutureStream missing skipped elements
	 * @see org.jooq.lambda.Seq#skip(long)
	 */
	@Override
	default LazyFutureStream<U> skip(long n) {
		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withStream(lastActive.stream().skip(n));
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
		return new LazyReact().reactToCollection(Arrays.asList(array));
	}

	
	/* 
	 *	@return distinct elements in this Stream (must be a finite stream!)
	 *
	 * @see org.jooq.lambda.Seq#distinct()
	 */
	@Override
	default Seq<U> distinct() {
		return toQueue().stream().distinct();
	}

	/**
	 * Duplicate a Streams into two equivalent Streams.
	 * <p>
	 * <code><pre>
	 * // tuple((1, 2, 3), (1, 2, 3))
	 * Seq.of(1, 2, 3).duplicate()
	 * </pre></code>
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
	 * <p>
	 * <code><pre>
	 * // tuple((1, 3, 5), (2, 4, 6))
	 * Seq.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
	 * </pre></code>
	 *
	 * @see #partition(Stream, Predicate)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> partition(Predicate<? super U> predicate) {
		Tuple2<Seq<U>, Seq<U>> partitioned = FutureStream.super
				.partition(predicate);
		return new Tuple2(partitioned.v1, partitioned.v2);
	}

	/**
	 * Construct a SimpleReact Stage from a supplied array
	 * 
	 * @param array
	 *            Array of value to form the reactive stream / sequence
	 * @return SimpleReact Stage
	 */
	public static <U> LazyFutureStream<U> parallelOf(U... array) {
		return new LazyReact().reactToCollection(Arrays.asList(array));
	}

	/**
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact parallelBuilder() {
		return new LazyReact();
	}

	/**
	 * Construct a new LazyReact builder, with a new task executor and retry executor
	 * with configured number of threads 
	 * 
	 * @param parallelism Number of threads task executor should have
	 * @return LazyReact instance
	 */
	public static LazyReact parallelBuilder(int parallelism) {
		return LazyReact.builder().executor(new ForkJoinPool(parallelism))
				.retrier(new RetryBuilder().parallelism(parallelism)).build();
	}

	/**
	 * @return new LazyReact builder configured with standard parallel executor
	 * By default this is the ForkJoinPool common instance but is configurable in the ThreadPools class
	 * 
	 * @see ThreadPools#getStandard()
	 * @see RetryBuilder#getDefaultInstance()
	 */
	public static LazyReact parallelCommonBuilder() {
		return LazyReact.builder().executor(ThreadPools.getStandard())
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
	}

	/**
	 * @return new LazyReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * New ForkJoinPool will be created
	 */
	public static LazyReact sequentialBuilder() {
		return LazyReact.builder().executor(new ForkJoinPool(1))
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(Executors.newScheduledThreadPool(1))).build();
	}

	/**
	 * @return  LazyReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * Common free thread Executor from
	 */
	public static LazyReact sequentialCommonBuilder() {
		return LazyReact.builder().executor(ThreadPools.getCommonFreeThread())
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
	}

	/**
	 * @param executor
	 *            Executor this SimpleReact instance will use to execute
	 *            concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact lazy(ExecutorService executor) {
		return new LazyReact(executor);
	}

	/**
	 * @param retry
	 *            RetryExecutor this SimpleReact instance will use to retry
	 *            concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact lazy(RetryExecutor retry) {
		return LazyReact.builder().retrier(retry).build();
	}

	/**
	 * @param executor
	 *            Executor this SimpleReact instance will use to execute
	 *            concurrent tasks.
	 * @param retry
	 *            RetryExecutor this SimpleReact instance will use to retry
	 *            concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact lazy(ExecutorService executor, RetryExecutor retry) {
		return LazyReact.builder().executor(executor).retrier(retry).build();
	}

	/**
	 * @see Stream#of(Object)
	 */
	static <T> LazyFutureStream<T> of(T value) {
		return futureStream((Stream) Seq.of(value));
	}

	/**
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> LazyFutureStream<T> of(T... values) {
		return futureStream((Stream) Seq.of(values));
	}

	/**
	 * @see Stream#empty()
	 */
	static <T> LazyFutureStream<T> empty() {
		return futureStream((Stream) Seq.empty());
	}

	/**
	 * @see Stream#iterate(Object, UnaryOperator)
	 */
	static <T> LazyFutureStream<T> iterate(final T seed,
			final UnaryOperator<T> f) {
		return futureStream((Stream) Seq.iterate(seed, f));
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
		return futureStream(Stream.generate(s));
	}

	/**
	 * Wrap a Stream into a FutureStream.
	 */
	static <T> LazyFutureStream<T> futureStream(Stream<T> stream) {
		if (stream instanceof LazyFutureStream)
			return (LazyFutureStream<T>) stream;
		if (stream instanceof FutureStream)
			stream = ((FutureStream) stream).toQueue().stream();

		return new LazyFutureStreamImpl<T>(
				stream.map(CompletableFuture::completedFuture),
				ThreadPools.getSequential(), RetryBuilder
						.getDefaultInstance().withScheduler(
								ThreadPools.getSequentialRetry()));
	}

	/**
	 * Wrap an Iterable into a FutureStream.
	 */
	static <T> LazyFutureStream<T> futureStream(Iterable<T> iterable) {
		return futureStream(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream.
	 */
	static <T> LazyFutureStream<T> futureStream(Iterator<T> iterator) {
		return futureStream(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}

}
