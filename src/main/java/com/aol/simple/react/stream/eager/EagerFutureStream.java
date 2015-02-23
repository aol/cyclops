package com.aol.simple.react.stream.eager;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.EagerToQueue;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;
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
	 * Non-blocking asyncrhonous application of the supplied function.
	 * Equivalent to map from Streams / Seq apis.
	 * 
	 *	@param fn Function to be applied asynchronously
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#then(java.util.function.Function)
	 */
	default <R> EagerFutureStream<R> then(final Function<U, R> fn) {
		return (EagerFutureStream) FutureStream.super.then(fn);
	}

	/* 
	 * Merge two SimpleReact Streams
	 *	@param s Stream to merge
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#merge(com.aol.simple.react.stream.traits.SimpleReactStream)
	 */
	@Override
	default EagerFutureStream<U> merge(SimpleReactStream<U> s) {
		return (EagerFutureStream) FutureStream.super.merge(s);
	}

	/* 
	 * Define failure handling for this stage in a stream.
	 * Recovery function will be called after an excption
	 * Will be passed a SimpleReactFailedStageException which contains both the cause,
	 * and the input value.
	 *
	 *	@param fn Recovery function
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#onFail(java.util.function.Function)
	 */
	@Override
	default <U> EagerFutureStream<U> onFail(
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (EagerFutureStream) FutureStream.super.onFail(fn);
	}

	/* 
	 * Capture non-recoverable exception
	 * 
	 *	@param errorHandler Consumer that captures the exception
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#capture(java.util.function.Consumer)
	 */
	@Override
	default EagerFutureStream<U> capture(
			final Consumer<? extends Throwable> errorHandler) {
		return (EagerFutureStream) FutureStream.super.capture(errorHandler);
	}

	/* 
	 * @see com.aol.simple.react.stream.traits.FutureStream#allOf(java.util.function.Function)
	 */
	@Override
	default <T, R> EagerFutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (EagerFutureStream) FutureStream.super.allOf(fn);
	}

	/* 
	 * @see com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function.Consumer)
	 */
	@Override
	default EagerFutureStream<U> peek(final Consumer<? super U> consumer) {
		return (EagerFutureStream) FutureStream.super.peek(consumer);
	}

	/* 
	 * @see com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function.Predicate)
	 */
	default EagerFutureStream<U> filter(final Predicate<? super U> p) {
		return (EagerFutureStream) FutureStream.super.filter(p);
	}

	/*
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

	/*
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
	default <R> EagerFutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {

		return (EagerFutureStream) FutureStream.super
				.fromStreamCompletableFuture(stream);
	}

	
	/* 
	 * Take the first (maxSize) completed results from this stage of the Stream as input to the next stage.
	 * e.g.
	 * 
	 * EagerFutureStream.of(()->loadSlow(),()->loadMedium(),()->loadFast()).limit(2)
	 * 
	 * will take the results from loadMedium and loadFast()
	 * 
	 * 
	 *	@param maxSize The size of the subsequent Stream
	 *	@return EagerFutureStream
	 * @see org.jooq.lambda.Seq#limit(long)
	 */
	@Override
	default EagerFutureStream<U> limit(long maxSize) {
		return fromStream(toQueue().stream().limit(maxSize));
	}

	/* 
	 * Cast all elements in this stream to specified type. May throw {@link ClassCastException}.
	 * 
	 *  EagerFutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
	 *  
	 *  will throw a ClassCastException
	 *  
	 *	@param type Type to cast to
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.traits.FutureStream#cast(java.lang.Class)
	 */
	@Override
	default <U> EagerFutureStream<U> cast(Class<U> type) {
		return (EagerFutureStream<U>) FutureStream.super.cast(type);
	}
	
	 /**
     * Keep only those elements in a stream that are of a given type.
     * <p>
     * <code><pre>
     * 
     * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
     * 
     * gives a Stream of (1,2,3)
     * 
     * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
     * 
     * gives a Stream of ("a","b")
     * </pre></code>
     *  @see com.aol.simple.react.stream.traits.FutureStream#ofType(java.lang.Class)
     */
	@Override
	default <U> EagerFutureStream<U> ofType(Class<U> type){
		return (EagerFutureStream<U>)FutureStream.super.ofType(type);
	}
	
	/* 
	 * Skip the first (n) completed results from this stage of the Stream
	 * 
	 * e.g. 
	 * 
	 *  EagerFutureStream.of(()->loadSlow(),()->loadMedium(),()->loadFast()).limit(2)
	 * 
	 * will take the results from loadSlow()
	 * 
	 *	@param n number of Elements to skip
	 *	@return EagerFutureStream
	 * @see org.jooq.lambda.Seq#skip(long)
	 */
	@Override
	default EagerFutureStream<U> skip(long n) {
		return fromStream(toQueue().stream().skip(n));
	}

	/**
	 * Concatenate two streams.
	 * <p>
	 * <code><pre>
	 * // (1, 2, 3, 4, 5, 6)
	 * EagerFutureStream.of(1, 2, 3).concat(EagerFutureStream.of(4, 5, 6))
	 * </pre></code>
	 *
	 * @see #concat(Stream[])
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	default EagerFutureStream<U> concat(Stream<U> other) {
		if(other instanceof SimpleReactStream)
			return (EagerFutureStream)merge((SimpleReactStream)other);
		return fromStream(FutureStream.super.concat(other));
	}
	default FutureStream<U> concat(SimpleReactStream<U> other) {
		
		return (EagerFutureStream)merge((SimpleReactStream)other);
		
	}

	/**
	 * Concatenate two streams.
	 * <p>
	 * <code><pre>
	 * // (1, 2, 3, 4)
	 * EagerFutureStream.of(1, 2, 3).concat(4)
	 * </pre></code>
	 *
	 * @see #concat(Stream[])
	 */
	default EagerFutureStream<U> concat(U other) {
		return fromStream(FutureStream.super.concat(other));
	}

	/**
	 * Concatenate two streams.
	 * <p>
	 * <code><pre>
	 * // (1, 2, 3, 4, 5, 6)
	 * EagerFutureStream.of(1, 2, 3).concat(4, 5, 6)
	 * </pre></code>
	 *
	 * @see #concat(Stream[])
	 */
	@SuppressWarnings({ "unchecked" })
	default EagerFutureStream<U> concat(U... other) {
		return ( EagerFutureStream<U>)concat((SimpleReactStream)EagerFutureStream.of(other));
	}

	/**
	 * Repeat a stream infinitely.
	 * <p>
	 * <code><pre>
	 * // (1, 2, 3, 1, 2, 3, ...)
	 * EagerFutureStream.of(1, 2, 3).cycle();
	 * </pre></code>
	 *
	 * @see #cycle(Stream)
	 
	@Override
	default EagerFutureStream<U> cycle() {
		return fromStream(FutureStream.super.cycle());
	}*/
	  /**
     * Returns a limited interval from a given Stream.
     * <p>
     * <code><pre>
     * // (4, 5)
     * EagerFutureStream.of(1, 2, 3, 4, 5, 6).slice(3, 5)
     * </pre></code>
     *
     * @see #slice(Stream, long, long)
     */
    @Override
    default EagerFutureStream<U> slice(long from, long to) {
        return slice(from, to);
    }
	 
	/**
	 * Zip two streams into one.
	 * <p>
	 * <code><pre>
	 * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
	 * EagerFutureStream.of(1, 2, 3).zip(EagerFutureStream.of("a", "b", "c"))
	 * </pre></code>
	 *
	 * @see #zip(Stream, Stream)
	 */
	@Override
	default <R> EagerFutureStream<Tuple2<U, R>> zip(Seq<R> other) {
		return fromStream(FutureStream.super.zip(other));
	}

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting
	 * values.
	 * <p>
	 * <code><pre>
	 * // ("1:a", "2:b", "3:c")
	 * EagerFutureStream.of(1, 2, 3).zip(EagerFutureStream.of("a", "b", "c"), (i, s) -> i + ":" + s)
	 * </pre></code>
	 *
	 * @see #zip(Seq, BiFunction)
	 */
	@Override
	default <T, R> EagerFutureStream<R> zip(Seq<T> other,
			BiFunction<U, T, R> zipper) {
		return fromStream(FutureStream.super.zip(other, zipper));
	}

	/**
	 * Zip a Stream with a corresponding Stream of indexes.
	 * <p>
	 * <code><pre>
	 * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
	 * EagerFutureStream.of("a", "b", "c").zipWithIndex()
	 * </pre></code>
	 *
	 * @see #zipWithIndex(Stream)
	 */
	@Override
	default EagerFutureStream<Tuple2<U, Long>> zipWithIndex() {
		return fromStream(FutureStream.super.zipWithIndex());
	}

	/**
	 * Scan a stream to the left.
	 * <p>
	 * <code><pre>
	 * // ("", "a", "ab", "abc")
	 * EagerFutureStream.of("a", "b", "c").scanLeft("", (u, t) -> u + t)
	 * </pre></code>
	 */
	@Override
	default <T> EagerFutureStream<T> scanLeft(T seed,
			BiFunction<T, ? super U, T> function) {
		return fromStream(FutureStream.super.scanLeft(seed, function));
	}

	/**
	 * Scan a stream to the right.
	 * <p>
	 * <code><pre>
	 * // ("", "c", "cb", "cba")
	 * EagerFutureStream.of("a", "b", "c").scanRight("", (t, u) -> u + t)
	 * </pre></code>
	 */
	@Override
	default <R> EagerFutureStream<R> scanRight(R seed,
			BiFunction<? super U, R, R> function) {
		return fromStream(FutureStream.super.scanRight(seed, function));
	}

	/**
	 * Reverse a stream.
	 * <p>
	 * <code><pre>
	 * // (3, 2, 1)
	 * EagerFutureStream.of(1, 2, 3).reverse()
	 * </pre></code>
	 */
	@Override
	default EagerFutureStream<U> reverse() {
		return fromStream(FutureStream.super.reverse());
	}

	/**
	 * Shuffle a stream
	 * <p>
	 * <code><pre>
	 * // e.g. (2, 3, 1)
	 * EagerFutureStream.of(1, 2, 3).shuffle()
	 * </pre></code>
	 */
	@Override
	default EagerFutureStream<U> shuffle() {
		return fromStream(FutureStream.super.shuffle());
	}

	/**
	 * Shuffle a stream using specified source of randomness
	 * <p>
	 * <code><pre>
	 * // e.g. (2, 3, 1)
	 * EagerFutureStream.of(1, 2, 3).shuffle(new Random())
	 * </pre></code>
	 */
	@Override
	default EagerFutureStream<U> shuffle(Random random) {
		return fromStream(FutureStream.super.shuffle(random));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to <code>true</code>.
	 * <p>
	 * <code><pre>
	 * // (3, 4, 5)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).skipWhile(i -> i < 3)
	 * </pre></code>
	 *
	 * @see #skipWhile(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> skipWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipWhile(predicate));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to <code>false</code>.
	 * <p>
	 * <code><pre>
	 * // (3, 4, 5)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).skipUntil(i -> i == 3)
	 * </pre></code>
	 *
	 * @see #skipUntil(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> skipUntil(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipUntil(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to <code>true</code>.
	 * <p>
	 * <code><pre>
	 * // (1, 2)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).limitWhile(i -> i < 3)
	 * </pre></code>
	 *
	 * @see #limitWhile(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> limitWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.limitWhile(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to <code>false</code>.
	 * <p>
	 * <code><pre>
	 * // (1, 2)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).limitUntil(i -> i == 3)
	 * </pre></code>
	 *
	 * @see #limitUntil(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> limitUntil(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.limitUntil(predicate));
	}

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * <p>
	 * <code><pre>
	 * // (1, 0, 2, 0, 3, 0, 4)
	 * EagerFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * </pre></code>
	 *
	 * @see #intersperse(Stream, Object)
	 */
	@Override
	default EagerFutureStream<U> intersperse(U value) {
		return fromStream(FutureStream.super.intersperse(value));
	}

	/**
	 * Construct an Eager SimpleReact Stream from specified array
	 * 
	 * @param array
	 *            Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> EagerFutureStream<U> parallel(U... array) {
		return new EagerReact().reactToCollection(Arrays.asList(array));
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
	 * Duplicate a Streams into two equivalent Streams.
	 * <p>
	 * <code><pre>
	 * // tuple((1, 2, 3), (1, 2, 3))
	 * EagerFutureStream.of(1, 2, 3).duplicate()
	 * </pre></code>
	 *
	 * @see #duplicate(Stream)
	 */
	@Override
	default Tuple2<Seq<U>, Seq<U>> duplicate() {
		Tuple2<Seq<U>, Seq<U>> duplicated = FutureStream.super.duplicate();
		return new Tuple2(fromStream(duplicated.v1), fromStream(duplicated.v2));
	}

	/**
	 * Duplicate a Stream into two equivalent EagerFutureStreams
	 * 
	 *  EagerFutureStream.of(1, 2, 3).duplicate()
	 *  
	 *  results in 
	 *  
	 *  tuple((1,2,3),(1,2,3))
	 * 
	 * @return 
	 * 
	 * @see #duplicate()
	 */
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> duplicateFutureStream() {
		Tuple2 dup = duplicate();
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) dup;
	}

	/**
	 * Partition a stream into two given a predicate.
	 * <p>
	 * <code><pre>
	 * // tuple((1, 3, 5), (2, 4, 6))
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
	 * </pre></code>
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
	 * Partition an EagerFutureStream into two EagerFutureStreams given a predicate.
	 * 
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
	 * 
	 * results in 
	 * 
	 *  tuple((1, 3, 5), (2, 4, 6))
	 * 
	 * @param predicate Predicate to split Stream
	 * @return EagerFutureStream
	 * @see #partition(Predicate)
	 */
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> partitionFutureStream(
			Predicate<? super U> predicate) {
		Tuple2 partition = partition(predicate);
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) partition;
	}

	/**
	 * Split a stream at a given position.
	 * <p>
	 * <code><pre>
	 * // tuple((1, 2, 3), (4, 5, 6))
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitAt(3)
	 * </pre></code>
	 *
	 * @see #splitAt(Stream, long)
	 */
	default Tuple2<Seq<U>, Seq<U>> splitAt(long position) {
		Tuple2<Seq<U>, Seq<U>> split = FutureStream.super.splitAt(position);
		return new Tuple2(fromStream(split.v1), fromStream(split.v2));
	}

	/**
	 *  Split a EagerFutureStream at a given position.
	 * 
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitAt(3)
	 * 
	 * results in tuple((1, 2, 3), (4, 5, 6))
	 * 
	 * @see #splitAt(long)
	 */
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> splitAtFutureStream(
			long position) {
		Tuple2 split = splitAt(position);
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) split;
	}

	/**
	 * Split a stream at the head.
	 * <p>
	 * <code><pre>
	 * // tuple(1, (2, 3, 4, 5, 6))
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitHead(3)
	 * </pre></code>
	 *
	 * @see #splitAt(Stream, long)
	 */
	@Override
	default Tuple2<Optional<U>, Seq<U>> splitAtHead() {
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

	/**
	 * @return EagerReact for handling finite streams
	 * @see SimpleReact.SimpleReact()
	 */
	public static EagerReact parallelBuilder() {
		return new EagerReact();
	}

	/**
	 * Construct a new EagerReact builder, with a new task executor and retry executor
	 * with configured number of threads 
	 * 
	 * @param parallelism Number of threads task executor should have
	 * @return eager EagerReact instance
	 */
	public static EagerReact parallelBuilder(int parallelism) {
		return eagerBuilder(new ForkJoinPool(parallelism),
				new RetryBuilder().parallelism(parallelism));
	}
	/**
	 * @return new EagerReact builder configured with standard parallel executor
	 * By default this is the ForkJoinPool common instance but is configurable in the ThreadPools class
	 * 
	 * @see ThreadPools#getStandard()
	 * @see RetryBuilder#getDefaultInstance()
	 */
	public static EagerReact paraellelCommonBuilder() {
		return EagerReact.builder().executor(ThreadPools.getStandard())
		.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
	}
	/**
	 * @return new eager EagerReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * New ForkJoinPool will be created
	 */
	public static EagerReact sequentialBuilder() {
		return EagerReact.builder().executor(new ForkJoinPool(1))
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(Executors.newScheduledThreadPool(1))).build();
	}


	/**
	 * @return new EagerReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * Common free thread Executor from
	 */
	public static EagerReact sequentialCommonBuilder() {
		return EagerReact.builder().executor(ThreadPools.getCommonFreeThread())
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
	}

	/**
	 * @param executor
	 *            Executor this EagerReact instance will use to execute
	 *            concurrent tasks.
	 * @return EagerReact for handling finite streams
	 */
	public static EagerReact eagerBuilder(ExecutorService executor) {
		return new EagerReact(executor);
	}

	/**
	 * @param retry
	 *            RetryExecutor this SimpleReact instance will use to retry
	 *            concurrent tasks.
	 * @return EagerReact for handling finite streams
	 */
	public static EagerReact eagerBuilder(RetryExecutor retry) {
		return EagerReact.builder().retrier(retry).build();
	}

	/**
	 * @param executor
	 *            Executor this SimpleReact instance will use to execute
	 *            concurrent tasks.
	 * @param retry
	 *            RetryExecutor this SimpleReact instance will use to retry
	 *            concurrent tasks.
	 * @return EagerReact for handling finite streams
	 */
	public static EagerReact eagerBuilder(ExecutorService executor,
			RetryExecutor retry) {
		return EagerReact.builder().executor(executor).retrier(retry).build();
	}

	/**
	 * @see Stream#of(Object)
	 */
	static <T> EagerFutureStream<T> of(T value) {
		return futureStream((Stream) EagerFutureStream.of(value));
	}

	/**
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> EagerFutureStream<T> of(T... values) {
		return futureStream((Stream) Stream.of(values));
	}

	/**
	 * @see Stream#empty()
	 */
	static <T> EagerFutureStream<T> empty() {
		return futureStream((Stream) Seq.empty());
	}

	/**
	 * Wrap a Stream into a FutureStream.
	 */
	static <T> EagerFutureStream<T> futureStream(Stream<T> stream) {
		if (stream instanceof FutureStream)
			return (EagerFutureStream<T>) stream;

		return new EagerFutureStreamImpl<T>(
				stream.map(CompletableFuture::completedFuture),
				ThreadPools.getSequential(), RetryBuilder
						.getDefaultInstance().withScheduler(
								ThreadPools.getSequentialRetry()));
	}

	/**
	 * Wrap an Iterable into a FutureStream.
	 */
	static <T> EagerFutureStream<T> futureStream(Iterable<T> iterable) {
		return futureStream(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream.
	 */
	static <T> EagerFutureStream<T> futureStream(Iterator<T> iterator) {
		return futureStream(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}

}
