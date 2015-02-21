package com.aol.simple.react.stream.lazy;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.stream.ThreadPools;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#limit(long)
	 */

	@Override
	default Seq<U> limit(long maxSize) {

		return toQueue().stream().limit(maxSize);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#skip(long)
	 */

	@Override
	default Seq<U> skip(long n) {

		return toQueue().stream().skip(n);

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
	 * (non-Javadoc)
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

	public static LazyReact parallelBuilder(int parallelism) {
		return LazyReact.builder().executor(new ForkJoinPool(parallelism))
				.retrier(new RetryBuilder().parallelism(parallelism)).build();
	}

	public static LazyReact parallelCommonBuilder() {
		return new LazyReact(ForkJoinPool.commonPool());
	}

	public static LazyReact sequentialBuilder() {
		return lazy(new ForkJoinPool(1));
	}

	public static LazyReact sequentialCommonBuilder() {
		return lazy(ThreadPools.getCommonFreeThread());
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
				Executors.newFixedThreadPool(1), RetryBuilder
						.getDefaultInstance().withScheduler(
								Executors.newScheduledThreadPool(1)));
	}

	/**
	 * Wrap an Iterable into a FutureStream.
	 */
	static <T> FutureStream<T> futureStream(Iterable<T> iterable) {
		return futureStream(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream.
	 */
	static <T> FutureStream<T> futureStream(Iterator<T> iterator) {
		return futureStream(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}

}
