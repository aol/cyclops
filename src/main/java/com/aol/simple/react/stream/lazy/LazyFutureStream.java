package com.aol.simple.react.stream.lazy;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
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

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.async.Subscription;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyToQueue;
import com.aol.simple.react.stream.traits.SimpleReactStream;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */
public interface LazyFutureStream<U> extends FutureStream<U>, LazyToQueue<U> {

	
	LazyFutureStream<U> withTaskExecutor(ExecutorService e);
	LazyFutureStream<U> withRetrier(RetryExecutor retry);
	LazyFutureStream<U> withWaitStrategy(Consumer<CompletableFuture> c);
	LazyFutureStream<U> withEager(boolean eager);
	LazyFutureStream<U> withLazyCollector(LazyResultConsumer<U> lazy);
	LazyFutureStream<U> withQueueFactory(QueueFactory<U> queue);
	
	LazyFutureStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	LazyFutureStream<U> withSubscription(Continueable sub);
	
	LazyFutureStream<U> withLastActive(StreamWrapper streamWrapper);
	
	
	
	/**
	 * Override return type on SimpleReactStream
	 */
	
	/* 
	 * Non-blocking asyncrhonous application of the supplied function.
	 * Equivalent to map from Streams / Seq apis.
	 * 
	 *	@param fn Function to be applied asynchronously
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#then(java.util.function.Function)
	 */
	default <R> LazyFutureStream<R> then(final Function<U, R> fn) {
		return (LazyFutureStream) FutureStream.super.then(fn);
	}

	/* 
	 * Merge two SimpleReact Streams
	 *	@param s Stream to merge
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#merge(com.aol.simple.react.stream.traits.SimpleReactStream)
	 */
	@Override
	default LazyFutureStream<U> merge(SimpleReactStream<U> s) {
		return (LazyFutureStream) FutureStream.super.merge(s);
	}

	/* 
	 * Define failure handling for this stage in a stream.
	 * Recovery function will be called after an exception
	 * Will be passed a SimpleReactFailedStageException which contains both the cause,
	 * and the input value.
	 *
	 *	@param fn Recovery function
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#onFail(java.util.function.Function)
	 */
	@Override
	default LazyFutureStream<U> onFail(
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (LazyFutureStream) FutureStream.super.onFail(fn);
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
	default LazyFutureStream<U> onFail(Class<? extends Throwable> exceptionClass, final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (LazyFutureStream)FutureStream.super.onFail(exceptionClass,fn);
	}

	/* 
	 * Capture non-recoverable exception
	 * 
	 *	@param errorHandler Consumer that captures the exception
	 *	@return Next stage in stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#capture(java.util.function.Consumer)
	 */
	@Override
	default LazyFutureStream<U> capture(
			final Consumer<? extends Throwable> errorHandler) {
		return (LazyFutureStream) FutureStream.super.capture(errorHandler);
	}

	/* 
	 * @see com.aol.simple.react.stream.traits.FutureStream#allOf(java.util.function.Function)
	 */
	@Override
	default <T, R> LazyFutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (LazyFutureStream) FutureStream.super.allOf(fn);
	}
	default <R> LazyFutureStream<R> anyOf(
			Function<U, R> fn) {

		return (LazyFutureStream) FutureStream.super.anyOf( fn);
	}
	/* 
	 * @see com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function.Consumer)
	 */
	@Override
	default LazyFutureStream<U> peek(final Consumer<? super U> consumer) {
		return (LazyFutureStream) FutureStream.super.peek(consumer);
	}

	/* 
	 * @see com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function.Predicate)
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
	default <R> LazyFutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {

		return (LazyFutureStream) FutureStream.super
				.fromStreamCompletableFuture(stream);
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
	default LazyFutureStream<U> concat(Stream<U> other) {
		
		
		SimpleReactStream stream = other instanceof SimpleReactStream? (SimpleReactStream)other : SimpleReactStream.sequentialCommonBuilder().fromStreamWithoutFutures(other);
		return (LazyFutureStream)merge(stream);
	}
	
	
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
     *  @see com.aol.simple.react.stream.traits.FutureStream#ofType(java.lang.Class)
     */
	@Override
	default <U> FutureStream<U> ofType(Class<U> type){
		return (LazyFutureStream<U>)FutureStream.super.ofType(type);
	}
	 
	 /**
     * Returns a stream with a given value interspersed between any two values of this stream.
     * 
     * 
     * // (1, 0, 2, 0, 3, 0, 4)
     * LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
     * 
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

		Continueable  sub = this.getSubscription();
		sub.registerLimit(maxSize);
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
		Continueable sub = this.getSubscription();
		sub.registerSkip(n);
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
		
		return toQueue().stream(getSubscription()).distinct();
	}

	/**
	 * Duplicate a Streams into two equivalent Streams.
	 * 
	 * 
	 * // tuple((1, 2, 3), (1, 2, 3))
	 * LazyFutureStream.of(1, 2, 3).duplicate()
	 * 
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
	 * // tuple((1, 3, 5), (2, 4, 6))
	 * LazyFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -&gt; i % 2 != 0)
	 *
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
     * Zip two streams into one.
     * <p>
     * <code><pre>
     * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
     * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"))
     * </pre></code>
     *
     * @see #zip(Stream, Stream)
     */
    default <T> Seq<Tuple2<U, T>> zip(Seq<T> other) {
        return zip(this, other);
    }

    /**
     * Zip two streams into one using a {@link BiFunction} to produce resulting values.
     * <p>
     * <code><pre>
     * // ("1:a", "2:b", "3:c")
     * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -> i + ":" + s)
     * </pre></code>
     *
     * @see #zip(Seq, BiFunction)
     */
    default <T, R> Seq<R> zip(Seq<T> other, BiFunction<U, T, R> zipper) {
        return zip(this, other, zipper);
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
	 * see RetryBuilder#getDefaultInstance()
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
			stream = ((FutureStream) stream).toQueue().stream(((FutureStream) stream).getSubscription());

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
	
	 /**
     * Zip two streams into one.
     * <p>
     * <code><pre>
     * // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
     * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"))
     * </pre></code>
     */
    static <T1, T2> Seq<Tuple2<T1, T2>> zip(Stream<T1> left, Stream<T2> right) {
        return zip(left, right, Tuple::tuple);
    }

    /**
     * Zip two streams into one using a {@link BiFunction} to produce resulting values.
     * <p>
     * <code><pre>
     * // ("1:a", "2:b", "3:c")
     * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -> i + ":" + s)
     * </pre></code>
     */
    static <T1, T2, R> Seq<R> zip(Stream<T1> left, Stream<T2> right, BiFunction<T1, T2, R> zipper) {
        final Iterator<T1> it1 = left.iterator();
        final Iterator<T2> it2 = right.iterator();

        class Zip implements Iterator<R> {
            @Override
            public boolean hasNext() {
            	if(!it1.hasNext()){
            		close(it2);
            	}
            	if(!it2.hasNext()){
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
	
    static void close(Iterator it){
    	
    	if(it instanceof CloseableIterator){
    		((CloseableIterator)it).close();
    	}
    }
    
}
