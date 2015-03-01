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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.StreamWrapper;
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
	default  EagerFutureStream<U> onFail(
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (EagerFutureStream) FutureStream.super.onFail(fn);
	}

	/* 
	 * Handle failure for a particular class of exceptions only
	 * 
	 *	@param exceptionClass Class of exceptions to handle
	 *	@param fn recovery function
	 *	@return recovered value
	 * @see com.aol.simple.react.stream.traits.FutureStream#onFail(java.lang.Class, java.util.function.Function)
	 */
	@Override
	default EagerFutureStream<U> onFail(Class<? extends Throwable> exceptionClass, final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (EagerFutureStream)FutureStream.super.onFail(exceptionClass,fn);
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
	default <R> EagerFutureStream<R> anyOf(
			Function<U, R> fn) {

		return (EagerFutureStream) FutureStream.super.anyOf( fn);
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
	default <R> EagerFutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {

		return (EagerFutureStream) FutureStream.super
				.fromStreamCompletableFuture(stream);
	}

	
	/* 
	 * Take the first (maxSize) completed results from this stage of the Stream as input to the next stage.
	 * e.g.
	 * 
	 * EagerFutureStream.of(()&gt;loadSlow(),()&gt;loadMedium(),()&gt;loadFast()).limit(2)
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
	
	default EagerFutureStream<U> limitFutures(long maxSize) {

		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withList(lastActive.stream().limit(maxSize).collect(Collectors.toList()));
		return this.withLastActive(limited);

	}

	default EagerFutureStream<U> skipFutures(long n){
		StreamWrapper lastActive = getLastActive();
		StreamWrapper limited = lastActive.withList(lastActive.stream().skip(n).collect(Collectors.toList()));
		return this.withLastActive(limited);
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
     * 
     * 
     * 
     * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
     * 
     * gives a Stream of (1,2,3)
     * 
     * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
     * 
     * gives a Stream of ("a","b")
     * 
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
	 *  EagerFutureStream.of(()&gt;loadSlow(),()&gt;loadMedium(),()&gt;loadFast()).limit(2)
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
	 * 
	 * 
	 * // (1, 2, 3, 4, 5, 6)
	 * EagerFutureStream.of(1, 2, 3).concat(EagerFutureStream.of(4, 5, 6))
	 * 
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
	 * 
	 * 
	 * // (1, 2, 3, 4)
	 * EagerFutureStream.of(1, 2, 3).concat(4)
	 * 
	 *
	 * @see #concat(Stream[])
	 */
	default EagerFutureStream<U> concat(U other) {
		return fromStream(FutureStream.super.concat(other));
	}

	/**
	 * Concatenate two streams.
	 * 
	 * 
	 * // (1, 2, 3, 4, 5, 6)
	 * EagerFutureStream.of(1, 2, 3).concat(4, 5, 6)
	 * 
	 *
	 * @see #concat(Stream[])
	 */
	@SuppressWarnings({ "unchecked" })
	default EagerFutureStream<U> concat(U... other) {
		return ( EagerFutureStream<U>)concat((SimpleReactStream)EagerFutureStream.of(other));
	}

	/**
	 * Repeat a stream infinitely.
	 * 
	 * 
	 * // (1, 2, 3, 1, 2, 3, ...)
	 * EagerFutureStream.of(1, 2, 3).cycle();
	 * 
	 *
	 * @see #cycle(Stream)
	 
	@Override
	default EagerFutureStream<U> cycle() {
		return fromStream(FutureStream.super.cycle());
	}*/
	  /**
     * Returns a limited interval from a given Stream.
     * 
     * 
     * // (4, 5)
     * EagerFutureStream.of(1, 2, 3, 4, 5, 6).slice(3, 5)
     * 
     *
     * @see #slice(Stream, long, long)
     */
   
    default EagerFutureStream<U> sliceFutures(long from, long to) {
    	List noType = Seq.seq(getLastActive().stream()).slice(from,to).collect(Collectors.toList());
        return  fromListCompletableFuture(noType);
    }
    @Override
    default EagerFutureStream<U> slice(long from, long to) {
    	
        return fromStream( FutureStream.super.slice(from, to));
    }
   
	 
	/**
	 * Zip two streams into one.
	 * 
	 * 
	 * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
	 * EagerFutureStream.of(1, 2, 3).zip(EagerFutureStream.of("a", "b", "c"))
	 * 
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
	 * 
	 * 
	 * // ("1:a", "2:b", "3:c")
	 * EagerFutureStream.of(1, 2, 3).zip(EagerFutureStream.of("a", "b", "c"), (i, s) &gt; i + ":" + s)
	 * 
	 *
	 * @see #zip(Seq, BiFunction)
	 */
	default <T, R> EagerFutureStream<R> zip(Seq<T> other,
			BiFunction<U, T, R> zipper) {
		//non-blocking via Queue
		return fromStream(FutureStream.super.zip(other, zipper));
	}
	
	//futures are embedded inside the Tuple making them unsuiatable for current SimpleReactStream
	
	default <R> Seq<Tuple2<CompletableFuture<U>, R>> zipFutures(Stream<R> other) {
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other));
		return ( Seq<Tuple2<CompletableFuture<U>, R>>)seq;
		
	}
	

	
	/**
	 * Zip a Stream with a corresponding Stream of indexes.
	 * 
	 * 
	 * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
	 * EagerFutureStream.of("a", "b", "c").zipWithIndex()
	 * 
	 *
	 * @see #zipWithIndex(Stream)
	 */
	default Seq<Tuple2<CompletableFuture<U>, Long>> zipFuturesWithIndex() {
		
		Seq seq = Seq.seq(getLastActive().stream().iterator()).zipWithIndex();
		return (Seq<Tuple2<CompletableFuture<U>, Long>>)seq;
	}
	/**
	 * Zip a Stream with a corresponding Stream of indexes.
	 * 
	 * 
	 * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
	 * EagerFutureStream.of("a", "b", "c").zipWithIndex()
	 * 
	 *
	 * @see #zipWithIndex(Stream)
	 */
	default EagerFutureStream<Tuple2<U, Long>> zipWithIndex() {
		return fromStream(FutureStream.super.zipWithIndex());
	}

	/**
	 * Scan a stream to the left.
	 * 
	 * 
	 * // ("", "a", "ab", "abc")
	 * EagerFutureStream.of("a", "b", "c").scanLeft("", (u, t) &gt; u + t)
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
	 * 
	 * // ("", "c", "cb", "cba")
	 * EagerFutureStream.of("a", "b", "c").scanRight("", (t, u) &gt; u + t)
	 * 
	 */
	@Override
	default <R> EagerFutureStream<R> scanRight(R seed,
			BiFunction<? super U, R, R> function) {
		return fromStream(FutureStream.super.scanRight(seed, function));
	}

	/**
	 * Reverse a stream.
	 * 
	 * 
	 * // (3, 2, 1)
	 * EagerFutureStream.of(1, 2, 3).reverse()
	 * 
	 */
	@Override
	default EagerFutureStream<U> reverse() {
		return fromStream(FutureStream.super.reverse());
	}

	/**
	 * Shuffle a stream
	 * 
	 * 
	 * // e.g. (2, 3, 1)
	 * EagerFutureStream.of(1, 2, 3).shuffle()
	 * 
	 */
	@Override
	default EagerFutureStream<U> shuffle() {
		return fromStream(FutureStream.super.shuffle());
	}

	/**
	 * Shuffle a stream using specified source of randomness
	 * 
	 * 
	 * // e.g. (2, 3, 1)
	 * EagerFutureStream.of(1, 2, 3).shuffle(new Random())
	 * 
	 */
	@Override
	default EagerFutureStream<U> shuffle(Random random) {
		return fromStream(FutureStream.super.shuffle(random));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to true.
	 * 
	 * 
	 * // (3, 4, 5)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).skipWhile(i &gt; i &lt; 3)
	 * 
	 *
	 * @see #skipWhile(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> skipWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipWhile(predicate));
	}

	/**
	 * Returns a stream with all elements skipped for which a predicate
	 * evaluates to false.
	 * 
	 * 
	 * // (3, 4, 5)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).skipUntil(i &gt; i == 3)
	 * 
	 *
	 * @see #skipUntil(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> skipUntil(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.skipUntil(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to true.
	 * 
	 * 
	 * // (1, 2)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
	 * 
	 *
	 * @see #limitWhile(Stream, Predicate)
	 */
	@Override
	default EagerFutureStream<U> limitWhile(Predicate<? super U> predicate) {
		return fromStream(FutureStream.super.limitWhile(predicate));
	}

	/**
	 * Returns a stream limited to all elements for which a predicate evaluates
	 * to false.
	 * 
	 * 
	 * // (1, 2)
	 * EagerFutureStream.of(1, 2, 3, 4, 5).limitUntil(i &gt; i == 3)
	 * 
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
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4)
	 * EagerFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * 
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
	 * Duplicate a Streams into two equivalent Streams. (Operates on underlying futures)
	 * 
	 * 
	 * // tuple((1, 2, 3), (1, 2, 3))
	 * EagerFutureStream.of(1, 2, 3).duplicate()
	 * 
	 *
	 * @see #duplicate(Stream)
	 */
	
	default Tuple2<Seq<U>, Seq<U>> duplicateFutures() {
		//unblocking impl
		Stream stream = getLastActive().stream();
		Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>>  duplicated = Seq.seq((Stream<CompletableFuture<U>>)stream).duplicate();
		return new Tuple2(fromStreamCompletableFuture(duplicated.v1), fromStreamCompletableFuture(duplicated.v2));
	}
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
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> duplicateFuturesFutureStream() {
		Tuple2 dup = duplicateFutures();
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) dup;
	}
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> duplicateFutureStream() {
		Tuple2 dup = duplicate();
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) dup;
	}

	/**
	 * Partition a stream into two given a predicate. (Operates on results, not futures)
	 * 
	 * 
	 * // tuple((1, 3, 5), (2, 4, 6))
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -&gt; i % 2 != 0)
	 * 
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
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -&gt; i % 2 != 0)
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
	 * Split a stream at a given position. (Operates on futures)
	 * 
	 * 
	 * // tuple((1, 2, 3), (4, 5, 6))
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitAt(3)
	 * 
	 *
	 * @see #splitAt(Stream, long)
	 */
	default Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> splitAtNonBlocking(long position) {
		Stream stream = getLastActive().stream();
		Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>>  split = Seq.seq((Stream<CompletableFuture<U>>)stream).splitAt(position);

	
		return split;
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
	default Tuple2<Seq<U>, Seq<U>> splitAt(long position) {
		//blocking impl

		Tuple2<Seq<U>, Seq<U>> split = FutureStream.super.splitAt(position);
		return new Tuple2(fromStream(split.v1), fromStream(split.v2));
	}

	
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> splitAtNonBlockingFutureStream(
			long position) {
		Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> split = splitAtNonBlocking(position);
	 return new Tuple2(fromListCompletableFuture(split.v1.collect(Collectors.toList())),fromListCompletableFuture(split.v2.collect(Collectors.toList())));
	}
	default Tuple2<EagerFutureStream<U>, EagerFutureStream<U>> splitAtFutureStream(
			long position) {
		Tuple2 split = splitAt(position);
		return (Tuple2<EagerFutureStream<U>, EagerFutureStream<U>>) split;
	}
	
	default <R> EagerFutureStream<R> fromListCompletableFuture(
			List<CompletableFuture<R>> list) {
		
		return (EagerFutureStream)FutureStream.super.fromListCompletableFuture(list);
	}

	/**
	 * Split a stream at the head. 
	 * 
	 * 
	 * // tuple(1, (2, 3, 4, 5, 6))
	 * EagerFutureStream.of(1, 2, 3, 4, 5, 6).splitHead(3)
	 * 
	 *
	 * @see #splitAt(Stream, long)
	 */
	@Override
	default Tuple2<Optional<U>, Seq<U>> splitAtHead() {
		//blocking
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
	 * @see SimpleReact#SimpleReact()
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
	 * see RetryBuilder#getDefaultInstance()
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
