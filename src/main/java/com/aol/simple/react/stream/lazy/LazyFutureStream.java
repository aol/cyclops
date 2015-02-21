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
import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */
public interface LazyFutureStream<U> extends FutureStream<U>{

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#limit(long)
	 */
	@Override
	default  Seq<U> limit(long maxSize) {
		return  toQueue().stream().limit(maxSize);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#skip(long)
	 */
	@Override
	default Seq<U> skip(long n) {
		return toQueue().stream().skip(n);
	}

	 /**
    * Concatenate two streams.
    * <p>
    * <code><pre>
    * // (1, 2, 3, 4, 5, 6)
    * Seq.of(1, 2, 3).concat(Seq.of(4, 5, 6))
    * </pre></code>
    *
    * @see #concat(Stream[])
    */
   @SuppressWarnings({ "unchecked" })
   @Override
   default Seq<U> concat(Stream<U> other) {
       return FutureStream.super.concat(other);
   }

   /**
    * Concatenate two streams.
    * <p>
    * <code><pre>
    * // (1, 2, 3, 4)
    * Seq.of(1, 2, 3).concat(4)
    * </pre></code>
    *
    * @see #concat(Stream[])
    */
   default Seq<U> concat(U other) {
       return FutureStream.super.concat(other);
   }

   /**
    * Concatenate two streams.
    * <p>
    * <code><pre>
    * // (1, 2, 3, 4, 5, 6)
    * Seq.of(1, 2, 3).concat(4, 5, 6)
    * </pre></code>
    *
    * @see #concat(Stream[])
    */
   @SuppressWarnings({ "unchecked" })
   default Seq<U> concat(U... other) {
       return concat(Seq.of(other));
   }

   /**
    * Repeat a stream infinitely.
    * <p>
    * <code><pre>
    * // (1, 2, 3, 1, 2, 3, ...)
    * Seq.of(1, 2, 3).cycle();
    * </pre></code>
    *
    * @see #cycle(Stream)
    */
   @Override
   default Seq<U> cycle() {
       return  FutureStream.super.cycle();
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
   @Override
   default <R> Seq<Tuple2<U, R>> zip(Seq<R> other) {
       return FutureStream.super.zip(other);
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
 	@Override
 	default <T, R> Seq<R> zip(Seq<T> other, BiFunction<U, T, R> zipper) {
       return FutureStream.super.zip( other, zipper);
   }

   /**
    * Zip a Stream with a corresponding Stream of indexes.
    * <p>
    * <code><pre>
    * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
    * Seq.of("a", "b", "c").zipWithIndex()
    * </pre></code>
    *
    * @see #zipWithIndex(Stream)
    */
   @Override
   default Seq<Tuple2<U, Long>> zipWithIndex() {
       return FutureStream.super.zipWithIndex();
   }

   
   /**
    * Scan a stream to the left.
    * <p>
    * <code><pre>
    * // ("", "a", "ab", "abc")
    * Seq.of("a", "b", "c").scanLeft("", (u, t) -> u + t)
    * </pre></code>
    */
   @Override
   default <T> Seq<T> scanLeft(T seed, BiFunction<T, ? super U, T> function) {
       return FutureStream.super.scanLeft(seed, function);
   }

   /**
    * Scan a stream to the right.
    * <p>
    * <code><pre>
    * // ("", "c", "cb", "cba")
    * Seq.of("a", "b", "c").scanRight("", (t, u) -> u + t)
    * </pre></code>
    */
   @Override
   default <R> Seq<R> scanRight(R seed, BiFunction<? super U, R, R> function) {
       return FutureStream.super.scanRight(seed, function);
   }

   /**
    * Reverse a stream.
    * <p>
    * <code><pre>
    * // (3, 2, 1)
    * Seq.of(1, 2, 3).reverse()
    * </pre></code>
    */
   @Override
   default  Seq<U> reverse() {
       return FutureStream.super.reverse();
   }

   /**
    * Shuffle a stream
    * <p>
    * <code><pre>
    * // e.g. (2, 3, 1)
    * Seq.of(1, 2, 3).shuffle()
    * </pre></code>
    */
  @Override
  default Seq<U> shuffle() {
       return FutureStream.super.shuffle();
   }

   /**
    * Shuffle a stream using specified source of randomness
    * <p>
    * <code><pre>
    * // e.g. (2, 3, 1)
    * Seq.of(1, 2, 3).shuffle(new Random())
    * </pre></code>
    */
   @Override
   default Seq<U> shuffle(Random random) {
       return FutureStream.super.shuffle(random);
   }

   /**
    * Returns a stream with all elements skipped for which a predicate evaluates to <code>true</code>.
    * <p>
    * <code><pre>
    * // (3, 4, 5)
    * Seq.of(1, 2, 3, 4, 5).skipWhile(i -> i < 3)
    * </pre></code>
    *
    * @see #skipWhile(Stream, Predicate)
    */
   @Override
   default Seq<U> skipWhile(Predicate<? super U> predicate) {
       return FutureStream.super.skipWhile(predicate);
   }

   /**
    * Returns a stream with all elements skipped for which a predicate evaluates to <code>false</code>.
    * <p>
    * <code><pre>
    * // (3, 4, 5)
    * Seq.of(1, 2, 3, 4, 5).skipUntil(i -> i == 3)
    * </pre></code>
    *
    * @see #skipUntil(Stream, Predicate)
    */
   @Override
   default Seq<U> skipUntil(Predicate<? super U> predicate) {
       return FutureStream.super.skipUntil(predicate);
   }

   /**
    * Returns a stream limited to all elements for which a predicate evaluates to <code>true</code>.
    * <p>
    * <code><pre>
    * // (1, 2)
    * Seq.of(1, 2, 3, 4, 5).limitWhile(i -> i < 3)
    * </pre></code>
    *
    * @see #limitWhile(Stream, Predicate)
    */
   @Override
   default Seq<U> limitWhile(Predicate<? super U> predicate) {
       return FutureStream.super.limitWhile(predicate);
   }

   /**
    * Returns a stream limited to all elements for which a predicate evaluates to <code>false</code>.
    * <p>
    * <code><pre>
    * // (1, 2)
    * Seq.of(1, 2, 3, 4, 5).limitUntil(i -> i == 3)
    * </pre></code>
    *
    * @see #limitUntil(Stream, Predicate)
    */
   @Override
   default Seq<U>  limitUntil(Predicate<? super U> predicate) {
       return FutureStream.super.limitUntil(predicate);
   }

   /**
    * Returns a stream with a given value interspersed between any two values of this stream.
    * <p>
    * <code><pre>
    * // (1, 0, 2, 0, 3, 0, 4)
    * Seq.of(1, 2, 3, 4).intersperse(0)
    * </pre></code>
    *
    * @see #intersperse(Stream, Object)
    */
   @Override
   default Seq<U> intersperse(U value) {
       return FutureStream.super.intersperse(value);
   }


	/**
	 * Construct an Lazy SimpleReact Stream from specified array
	 * 
	 * @param array Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> LazyFutureStream<U> parallel(U... array){
		return new LazyReact().reactToCollection(Arrays.asList(array));
	}
	
	/* (non-Javadoc)
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
        return new Tuple2(duplicated.v1,duplicated.v2);
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
    	Tuple2<Seq<U>, Seq<U>> partitioned = FutureStream.super.partition(predicate);
    	   return new Tuple2(partitioned.v1,partitioned.v2);
    }
   
   
	
	/**
	 * Construct a SimpleReact Stage from a supplied array
	 * 
	 * @param array Array of value to form the reactive stream / sequence
	 * @return SimpleReact Stage
	 */
	public static <U> LazyFutureStream<U> parallelOf(U... array){
		return new LazyReact().reactToCollection(Arrays.asList(array));
	}
	/**
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact parallelBuilder(){
		return new LazyReact();
	}
	
	public static LazyReact parallelBuilder(int parallelism){
		return  LazyReact.builder().executor(new ForkJoinPool(parallelism)).retrier(new RetryBuilder().parallelism( parallelism)).build();
	}
		
	public static LazyReact parallelCommonBuilder(){
		return new LazyReact(ForkJoinPool.commonPool());
	}
	
	public static LazyReact sequentialBuilder(){
		return lazy(new ForkJoinPool(1));
	}
	public static LazyReact sequentialCommonBuilder(){
		return lazy(ThreadPools.getCommonFreeThread());
	}
	
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact lazy(ExecutorService executor){
		return new LazyReact(executor);
	}
	
	/**
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact lazy(RetryExecutor retry){
		return LazyReact.builder().retrier(retry).build();
	}
	/**
	 *  @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static LazyReact lazy(ExecutorService executor, RetryExecutor retry){
		return LazyReact.builder().executor(executor).retrier(retry).build();
	}
	  
    /**
     * @see Stream#of(Object)
     */
    static <T> LazyFutureStream<T> of(T value) {
        return futureStream((Stream)Seq.of(value));
    }

    /**
     * @see Stream#of(Object[])
     */
    @SafeVarargs
    static <T> Seq<T> of(T... values) {
        return futureStream((Stream)Seq.of(values));
    }

    /**
     * @see Stream#empty()
     */
    static <T> LazyFutureStream<T> empty() {
        return futureStream((Stream)Seq.empty());
    }

    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> LazyFutureStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return futureStream((Stream)Seq.iterate(seed, f));
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
        if(stream instanceof FutureStream)
        	stream = ((FutureStream) stream).toQueue().stream();

        return new LazyFutureStreamImpl<T>(stream.map(CompletableFuture::completedFuture), Executors.newFixedThreadPool(1),
        		RetryBuilder.getDefaultInstance().withScheduler(Executors.newScheduledThreadPool(1)));
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
        return futureStream(StreamSupport.stream(spliteratorUnknownSize(iterator, ORDERED), false));
    }
	
}
