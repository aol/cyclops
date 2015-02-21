package com.aol.simple.react.stream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.api.Blockable;
import com.aol.simple.react.stream.api.Configuration;
import com.aol.simple.react.stream.api.LazyStream;
import com.aol.simple.react.stream.api.SimpleReactStream;

public interface FutureStream<U> extends Seq<U>,
										Configuration<U>, 
										LazyStream<U>,
										Blockable<U>,
										SimpleReactStream<U>{
	

	abstract boolean isEager();
	abstract  Queue<U> toQueue(); 
	abstract StreamWrapper getLastActive();
	abstract FutureStream<U> withLastActive(StreamWrapper streamWrapper);
	abstract <R> FutureStream<R> then(final Function<U, R> fn);
	abstract <R> FutureStream<R> fromStream(Stream<R> stream);
	abstract <R> R block(final Collector collector);
	
	
	/**
	 * Stream and Seq supporting methods
	 */

	


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
	default FutureStream<U> skip(long n) {
		return fromStream(toQueue().stream().skip(n));
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#forEach(java.util.function.Consumer)
	 */
	@Override
	default  void forEach(Consumer<? super U> action) {
		toQueue().stream().forEach((Consumer) action);

	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
	 */
	@Override
	default  void forEachOrdered(Consumer<? super U> action) {
		toQueue().stream().forEachOrdered((Consumer) action);

	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray()
	 */
	@Override
	default  Object[] toArray() {
		return toQueue().stream().toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
	 */
	@Override
	default  <A> A[] toArray(IntFunction<A[]> generator) {
		return toQueue().stream().toArray(generator);
	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	@Override
	default  U reduce(U identity, BinaryOperator<U> accumulator) {

		return (U) toQueue().stream().reduce(identity, accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.util.function.BinaryOperator)
	 */
	@Override
	default Optional<U> reduce(BinaryOperator<U> accumulator) {
		return toQueue().stream().reduce(accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#collect(java.util.function.Supplier,
	 * java.util.function.BiConsumer, java.util.function.BiConsumer)
	 */
	@Override
	default  <R> R collect(Supplier<R> supplier,
			BiConsumer<R, ? super U> accumulator, BiConsumer<R, R> combiner) {

		return (R) toQueue().stream().collect(supplier, accumulator, combiner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#collect(java.util.stream.Collector)
	
	@Override
	// <R, A> R collect(Collector<? super T, A, R> collector)
	default  <R, A> R collect(Collector<? super U, A, R> collector) {
		return block(collector);
	}
 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	@Override
	default Optional<U> min(Comparator<? super U> comparator) {

		return toQueue().stream().min(comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	@Override
	default Optional<U> max(Comparator<? super U> comparator) {
		return toQueue().stream().max(comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#count()
	 */
	@Override
	default long count() {

		return getLastActive().stream().count();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
	 */
	@Override
	default  boolean anyMatch(Predicate<? super U> predicate) {
		return toQueue().stream().anyMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
	 */
	@Override
	default  boolean allMatch(Predicate<? super U> predicate) {
		return toQueue().stream().allMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#noneMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean noneMatch(Predicate<? super U> predicate) {
		return toQueue().stream().noneMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findFirst()
	 */
	@Override
	default Optional<U> findFirst() {
		return toQueue().stream().findFirst();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findAny()
	 */
	@Override
	default Optional<U> findAny() {
		return toQueue().stream().findAny();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	@Override
	default  <R> R reduce(R identity, BiFunction<R, ? super U, R> accumulator,
			BinaryOperator<R> combiner) {

		return toQueue().stream().reduce(identity, accumulator, combiner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#stream()
	 */
	@Override
	default  Stream<U> stream() {
		return toQueue().stream();
	}
	@Override
	default <U> FutureStream<U> cast(Class<U> type) {
		return (FutureStream<U>) Seq.super.cast(type);
	}
	@Override
	default <U> FutureStream<U> ofType(Class<U> type){
		return (FutureStream<U>)Seq.super.ofType(type);
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
    default FutureStream<U> concat(Stream<U> other) {
        return fromStream(Seq.super.concat(other));
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
    default FutureStream<U> concat(U other) {
        return fromStream(Seq.super.concat(other));
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
    default FutureStream<U> concat(U... other) {
        return fromStream(concat(Seq.of(other)));
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
    default FutureStream<U> cycle() {
        return  fromStream(Seq.super.cycle());
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
    default <R> FutureStream<Tuple2<U, R>> zip(Seq<R> other) {
        return fromStream(Seq.super.zip(other));
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
  	default <T, R> FutureStream<R> zip(Seq<T> other, BiFunction<U, T, R> zipper) {
        return fromStream(Seq.super.zip( other, zipper));
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
    default FutureStream<Tuple2<U, Long>> zipWithIndex() {
        return fromStream(Seq.super.zipWithIndex());
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
    default <T> FutureStream<T> scanLeft(T seed, BiFunction<T, ? super U, T> function) {
        return fromStream(Seq.super.scanLeft(seed, function));
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
    default <R> FutureStream<R> scanRight(R seed, BiFunction<? super U, R, R> function) {
        return fromStream(Seq.super.scanRight(seed, function));
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
    default  FutureStream<U> reverse() {
        return fromStream(Seq.super.reverse());
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
   default FutureStream<U> shuffle() {
        return fromStream(Seq.super.shuffle());
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
    default FutureStream<U> shuffle(Random random) {
        return fromStream(Seq.super.shuffle(random));
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
    default FutureStream<U> skipWhile(Predicate<? super U> predicate) {
        return fromStream(Seq.super.skipWhile(predicate));
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
    default FutureStream<U> skipUntil(Predicate<? super U> predicate) {
        return fromStream(Seq.super.skipUntil(predicate));
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
    default FutureStream<U> limitWhile(Predicate<? super U> predicate) {
        return fromStream(Seq.super.limitWhile(predicate));
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
    default FutureStream<U>  limitUntil(Predicate<? super U> predicate) {
        return fromStream(Seq.super.limitUntil(predicate));
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
    default FutureStream<U> intersperse(U value) {
        return fromStream(Seq.super.intersperse(value));
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
    	Tuple2<Seq<U>, Seq<U>> duplicated = Seq.super.duplicate();
        return new Tuple2(fromStream(duplicated.v1),fromStream(duplicated.v2));
    }
    default Tuple2<FutureStream<U>, FutureStream<U>> duplicateFutureStream() {
    	Tuple2 dup = duplicate();
        return (Tuple2<FutureStream<U>, FutureStream<U>>)dup;
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
    	Tuple2<Seq<U>, Seq<U>> partitioned = Seq.super.partition(predicate);
    	   return new Tuple2(fromStream(partitioned.v1),fromStream(partitioned.v2));
    }
   
    default  Tuple2<FutureStream<U>, FutureStream<U>> partitionFutureStream(Predicate<? super U> predicate){
    	Tuple2 partition = partition(predicate);
        return (Tuple2<FutureStream<U>, FutureStream<U>>)partition;
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
    default Tuple2<Seq<U>, Seq<U>> splitAt(long position) {
    	Tuple2<Seq<U>, Seq<U>> split = Seq.super.splitAt(position);
        return new Tuple2(fromStream(split.v1),fromStream(split.v2));
    }
    default  Tuple2<FutureStream<U>, FutureStream<U>> splitAtFutureStream(long position){
    	Tuple2 split = splitAt(position);
        return (Tuple2<FutureStream<U>, FutureStream<U>>)split;
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
    default Tuple2<Optional<U>, Seq<U>> splitAtHead() {
    	 Tuple2<Optional<U>, Seq<U>> split = Seq.super.splitAtHead();
        return new Tuple2(split.v1,fromStream(split.v2));
    }
    
    default Tuple2<Optional<U>, FutureStream<U>> splitAtHeadFutureStream() {
    	Tuple2  split = splitAtHead();
    	return split;
    }
	
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#iterator()
	 */
	@Override
	default Iterator<U> iterator() {

		return toQueue().stream().iterator();
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#spliterator()
	 */
	@Override
	default Spliterator<U> spliterator() {
		return toQueue().stream().spliterator();
	}

	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#isParallel()
	 */
	@Override
	default boolean isParallel() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sequential()
	 */
	@Override
	default Seq<U> sequential() {
		Queue q = toQueue();
		q.fromStream(getLastActive().stream().map(it -> it.join()));
		q.close();
		return q.stream();
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#parallel()
	 */
	@Override
	default FutureStream<U> parallel() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#unordered()
	 */
	@Override
	default FutureStream<U> unordered() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
	 */
	@Override
	default Seq<U> onClose(Runnable closeHandler) {

		return Seq.seq(getLastActive().stream().onClose(closeHandler)
				.map(it -> (U) it.join()));
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#close()
	 */
	@Override
	default void close() {
		getLastActive().stream().close();

	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#map(java.util.function.Function)
	 */
	@Override
	default <R> FutureStream<R> map(Function<? super U, ? extends R> mapper) {
		return then((Function) mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#mapToInt(java.util.function.ToIntFunction)
	 */
	@Override
	default IntStream mapToInt(ToIntFunction<? super U> mapper) {
		return toQueue().stream().mapToInt(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#mapToLong(java.util.function.ToLongFunction)
	 */
	@Override
	default LongStream mapToLong(ToLongFunction<? super U> mapper) {
		return toQueue().stream().mapToLong(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#mapToDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	default DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) {
		return toQueue().stream().mapToDouble(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#flatMapToInt(java.util.function.Function)
	 */
	@Override
	default IntStream flatMapToInt(
			Function<? super U, ? extends IntStream> mapper) {
		return toQueue().stream().flatMapToInt(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#flatMapToLong(java.util.function.Function)
	 */
	@Override
	default LongStream flatMapToLong(
			Function<? super U, ? extends LongStream> mapper) {
		return toQueue().stream().flatMapToLong(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#flatMapToDouble(java.util.function.Function)
	 */
	@Override
	default DoubleStream flatMapToDouble(
			Function<? super U, ? extends DoubleStream> mapper) {
		return toQueue().stream().flatMapToDouble(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#distinct()
	 */
	@Override
	default FutureStream<U> distinct() {
		return fromStream(toQueue().stream().distinct());
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sorted()
	 */
	@Override
	default Seq<U> sorted() {
		return toQueue().stream().sorted();
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	default Seq<U> sorted(Comparator<? super U> comparator) {
		return toQueue().stream().sorted(comparator);
	}

	/**
	 * Give a function access to the current stage of a SimpleReact Stream
	 * @param consumer Consumer that will recieve current stage
	 * @return Self (current stage)
	 */
	default FutureStream<U> self(Consumer<FutureStream<U>> consumer) {
		return (FutureStream<U>) then((t) -> {
			consumer.accept(this);
			return (U) t;
		});
		
	}
	
	  /**
     * Returns a limited interval from a given Stream.
     * <p>
     * <code><pre>
     * // (4, 5)
     * Seq.of(1, 2, 3, 4, 5, 6).slice(3, 5)
     * </pre></code>
     *
     * @see #slice(Stream, long, long)
     */
    @Override
    default FutureStream<U> slice(long from, long to) {
        return fromStream(Seq.super.slice(from, to));
    }
    
    
    /**
     * @see Stream#of(Object)
     */
    static <T> FutureStream<T> of(T value) {
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
    static <T> FutureStream<T> empty() {
        return futureStream((Stream)Seq.empty());
    }

    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> FutureStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return futureStream((Stream)Seq.iterate(seed, f));
    }

    /**
     * @see Stream#generate(Supplier)
     */
    static FutureStream<Void> generate() {
        return generate(() -> null);
    }

    /**
     * @see Stream#generate(Supplier)
     */
    static <T> FutureStream<T> generate(T value) {
        return generate(() -> value);
    }

    /**
     * @see Stream#generate(Supplier)
     */
    static <T> FutureStream<T> generate(Supplier<T> s) {
        return futureStream(Stream.generate(s));
    }

    /**
     * Wrap a Stream into a FutureStream.
     */
    static <T> FutureStream<T> futureStream(Stream<T> stream) {
        if (stream instanceof FutureStream)
            return (FutureStream<T>) stream;

        return new FutureStreamImpl<T>(stream.map(it-> CompletableFuture.completedFuture(it)), Executors.newFixedThreadPool(1),
        		RetryBuilder.getDefaultInstance().withScheduler(Executors.newScheduledThreadPool(1)),false);
    }
    /**
     * Wrap a Stream into a FutureStream.
     */
    static <T> FutureStream<T> eagerFutureStream(Stream<T> stream) {
        if (stream instanceof FutureStream)
            return (FutureStream<T>) stream;

        return new FutureStreamImpl<T>(stream.map(it-> CompletableFuture.completedFuture(it)),null,null,false);
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
	
	/**
	 * Merge this reactive dataflow with another - recommended for merging
	 * different types. To merge flows of the same type the instance method
	 * merge is more appropriate.
	 * 
	 * @param s1
	 *            Reactive stage builder to merge
	 * @param s2
	 *            Reactive stage builder to merge
	 * @return Merged dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> FutureStream<R> merge(FutureStream s1, FutureStream s2) {
		List merged = Stream.of(s1.getLastActive().list(), s2.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (FutureStream<R>) s1.withLastActive(new StreamWrapper(merged));
	}
	

}
