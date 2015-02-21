package com.aol.simple.react.stream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.api.SyncToQueue;
import com.nurkiewicz.asyncretry.RetryExecutor;


public class EagerFutureStreamImpl<U> extends FutureStreamImpl<U> implements EagerFutureStream<U> {
	
	EagerFutureStreamImpl(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor, final RetryExecutor retrier) {
		super(stream,executor,retrier,true);
	}
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#limit(long)
	 */
	@Override
	public FutureStream<U> limit(long maxSize) {
		return  EagerFutureStream.super.limit(maxSize);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#skip(long)
	 */
	@Override
	public FutureStream<U> skip(long n) {
		return EagerFutureStream.super.skip(n);
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
   public FutureStream<U> concat(Stream<U> other) {
       return EagerFutureStream.super.concat(other);  
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
   public FutureStream<U> concat(U other) {
       return EagerFutureStream.super.concat(other);
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
   public FutureStream<U> concat(U... other) {
       return EagerFutureStream.super.concat(other);
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
   public FutureStream<U> cycle() {
       return  EagerFutureStream.super.cycle();
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
   public <R> FutureStream<Tuple2<U, R>> zip(Seq<R> other) {
       return EagerFutureStream.super.zip(other);
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
 	public <T, R> FutureStream<R> zip(Seq<T> other, BiFunction<U, T, R> zipper) {
       return EagerFutureStream.super.zip(other,zipper);
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
   public FutureStream<Tuple2<U, Long>> zipWithIndex() {
       return EagerFutureStream.super.zipWithIndex();
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
   public <T> FutureStream<T> scanLeft(T seed, BiFunction<T, ? super U, T> function) {
       return EagerFutureStream.super.scanLeft(seed,function);
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
   public <R> FutureStream<R> scanRight(R seed, BiFunction<? super U, R, R> function) {
       return EagerFutureStream.super.scanRight(seed, function);
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
   public  FutureStream<U> reverse() {
       return EagerFutureStream.super.reverse();
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
  public FutureStream<U> shuffle() {
       return EagerFutureStream.super.shuffle();
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
   public FutureStream<U> shuffle(Random random) {
       return EagerFutureStream.super.shuffle(random);
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
   public FutureStream<U> skipWhile(Predicate<? super U> predicate) {
       return EagerFutureStream.super.skipWhile(predicate);
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
   public FutureStream<U> skipUntil(Predicate<? super U> predicate) {
       return EagerFutureStream.super.skipUntil(predicate);
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
   public FutureStream<U> limitWhile(Predicate<? super U> predicate) {
       return EagerFutureStream.super.limitWhile(predicate);
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
   public FutureStream<U>  limitUntil(Predicate<? super U> predicate) {
       return EagerFutureStream.super.limitUntil(predicate);
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
   public FutureStream<U> intersperse(U value) {
       return EagerFutureStream.super.intersperse(value);
   }


	/**
	 * Construct an Eager SimpleReact Stream from specified array
	 * 
	 * @param array Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> FutureStream<U> parallel(U... array){
		return new SimpleReact().reactToCollection(Arrays.asList(array));
	}
	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#distinct()
	 */
	@Override
	public FutureStream<U> distinct() {
		return fromStream(toQueue().stream().distinct());
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
    public Tuple2<Seq<U>, Seq<U>> duplicate() {
    	return EagerFutureStream.super.duplicate();
      
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
    public Tuple2<Seq<U>, Seq<U>> partition(Predicate<? super U> predicate) {
    	return EagerFutureStream.super.partition(predicate);
    }
    
    /**
     * Split a stream at a given position.
     * <p>
     * <code><pre>
     * // tuple((1, 2, 3), (4, 5, 6))
     * Seq.of(1, 2, 3, 4, 5, 6).splitAt(3)
     * </pre></code>
     *
     * @see #splitAt(Stream, long)
     */
    public Tuple2<Seq<U>, Seq<U>> splitAt(long position) {
    	return EagerFutureStream.super.splitAt(position);
    }
    /**
     * Split a stream at the head.
     * <p>
     * <code><pre>
     * // tuple(1, (2, 3, 4, 5, 6))
     * Seq.of(1, 2, 3, 4, 5, 6).splitHead(3)
     * </pre></code>
     *
     * @see #splitAt(Stream, long)
     */
    @Override
    public Tuple2<Optional<U>, Seq<U>> splitAtHead() {
    	 return EagerFutureStream.super.splitAtHead();
    }
	
	
	
}
