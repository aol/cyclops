package com.aol.simple.react.stream.traits;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.StreamWrapper;

public interface FutureStream<U> extends Seq<U>,
										ConfigurableStream<U>, 
										LazyStream<U>,
										BlockingStream<U>,
										SimpleReactStream<U>,
										ToQueue<U>{
	

	
	
	
	
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#retry(java.util.function.Function)
	 */
	default <R> FutureStream<R> retry(final Function<U, R> fn){
		return (FutureStream)SimpleReactStream.super.retry(fn);
	}
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#allOf(java.util.stream.Collector, java.util.function.Function)
	 */
	default <T, R> FutureStream<R> allOf(final Collector collector,
			final Function<T, R> fn){
		return (FutureStream)SimpleReactStream.super.allOf(collector,fn);
	}
	default <R> FutureStream<R> anyOf(final Function<U,R> fn){
		return (FutureStream)SimpleReactStream.super.anyOf(fn);
	}
	
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#fromStreamCompletableFuture(java.util.stream.Stream)
	 */
	default <R> FutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {
		return (FutureStream)SimpleReactStream.super.fromStreamCompletableFuture(stream);
	}
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#then(java.util.function.Function)
	 */
	default <R> FutureStream<R> then(final Function<U, R> fn){
		return (FutureStream)SimpleReactStream.super.then(fn);
	}
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#fromStream(java.util.stream.Stream)
	 */
	default <R> FutureStream<R> fromStream(Stream<R> stream){
		return (FutureStream)SimpleReactStream.super.fromStream(stream);
	}
	
	/* 
	 * @see org.jooq.lambda.Seq#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> FutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn){
		return (FutureStream)SimpleReactStream.super.flatMap(flatFn);
	}
	
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#merge(com.aol.simple.react.stream.traits.SimpleReactStream)
	 */
	@Override
	default FutureStream<U> merge(SimpleReactStream<U> s){
		return (FutureStream)SimpleReactStream.super.merge(s);
	}
	
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#onFail(java.util.function.Function)
	 */
	@Override
	default FutureStream<U> onFail(final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (FutureStream)SimpleReactStream.super.onFail(fn);
	}
	
	/* 
	 *
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#onFail(java.lang.Class, java.util.function.Function)
	 */
	default FutureStream<U> onFail(Class<? extends Throwable> exceptionClass, final Function<? extends SimpleReactFailedStageException, U> fn){
		return (FutureStream)SimpleReactStream.super.onFail(exceptionClass,fn);
	}
	
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#capture(java.util.function.Consumer)
	 */
	@Override
	default  FutureStream<U> capture(final Consumer<? extends Throwable> errorHandler) {
		return (FutureStream)SimpleReactStream.super.capture(errorHandler);
	}
	/* 
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#allOf(java.util.function.Function)
	 */
	@Override
	default <T, R> FutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (FutureStream)SimpleReactStream.super.allOf(fn);
	}

	/* 
	 * @see org.jooq.lambda.Seq#peek(java.util.function.Consumer)
	 */
	@Override
	default FutureStream<U> peek(final Consumer<? super U> consumer){
		return (FutureStream)SimpleReactStream.super.peek(consumer);
	}
	/* 
	 * @see org.jooq.lambda.Seq#filter(java.util.function.Predicate)
	 */
	default FutureStream<U> filter(final Predicate<? super U> p){
		return (FutureStream)SimpleReactStream.super.filter(p);
	}
	
	/**
	 * Stream  supporting methods
	 */
	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#forEach(java.util.function.Consumer)
	 */
	@Override
	default  void forEach(Consumer<? super U> action) {
		toQueue().stream(getSubscription()).forEach((Consumer) action);

	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
	 */
	@Override
	default  void forEachOrdered(Consumer<? super U> action) {
		toQueue().stream(getSubscription()).forEachOrdered((Consumer) action);

	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray()
	 */
	@Override
	default  Object[] toArray() {
		return toQueue().stream(getSubscription()).toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
	 */
	@Override
	default  <A> A[] toArray(IntFunction<A[]> generator) {
		return toQueue().stream(getSubscription()).toArray(generator);
	}

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	@Override
	default  U reduce(U identity, BinaryOperator<U> accumulator) {

		return (U) toQueue().stream(getSubscription()).reduce(identity, accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.util.function.BinaryOperator)
	 */
	@Override
	default Optional<U> reduce(BinaryOperator<U> accumulator) {
		return toQueue().stream(getSubscription()).reduce(accumulator);
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

		return (R) toQueue().stream(getSubscription()).collect(supplier, accumulator, combiner);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	@Override
	default Optional<U> min(Comparator<? super U> comparator) {

		return toQueue().stream(getSubscription()).min(comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	@Override
	default Optional<U> max(Comparator<? super U> comparator) {
		return toQueue().stream(getSubscription()).max(comparator);
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
		return toQueue().stream(getSubscription()).anyMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
	 */
	@Override
	default  boolean allMatch(Predicate<? super U> predicate) {
		return toQueue().stream(getSubscription()).allMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#noneMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean noneMatch(Predicate<? super U> predicate) {
		return toQueue().stream(getSubscription()).noneMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findFirst()
	 */
	@Override
	default Optional<U> findFirst() {
		return toQueue().stream(getSubscription()).findFirst();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findAny()
	 */
	@Override
	default Optional<U> findAny() {
		return toQueue().stream(getSubscription()).findAny();
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

		return toQueue().stream(getSubscription()).reduce(identity, accumulator, combiner);
	}
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#iterator()
	 */
	@Override
	default Iterator<U> iterator() {

		if(getSubscription().closed())
			return Arrays.<U>asList().iterator();
		return new CloseableIterator<>(toQueue().stream(getSubscription()).iterator(),getSubscription());
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#spliterator()
	 */
	@Override
	default Spliterator<U> spliterator() {
		return toQueue().stream(getSubscription()).spliterator();
	}

	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#isParallel()
	 */
	@Override
	default boolean isParallel() {
		return true;
	}

	/* 
	 * Creates a sequential instance by populating an async Queue from the current stream,
	 * and reading sequentially from that Stream.
	 * 
	 * For an alternative approach change the task executors to single thread model, via withTaskExecutor and withRetrier
	 * 
	 *	@return Sequential Stream
	 * @see com.aol.simple.react.stream.traits.FutureStream#sequential()
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
	
	@Override
	default  Stream<U> stream() {
		return toQueue().stream(getSubscription());
	}
	
	
	/*
	 * Seq supporting methods
	 */
	
	/**
	 * native Seq
	 * 
	 */
	
	
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
    default FutureStream<U> intersperse(U value) {
        return intersperse(this, value);
    }

	
	
	
	/*
	 * 
	 *	@param type
	 *	@return
	 * @see org.jooq.lambda.Seq#cast(java.lang.Class)
	 */
	
	@Override
	default <U> FutureStream<U> cast(Class<U> type) {
		return (FutureStream<U>)cast(this,type);
	}
	@Override
	default <U> FutureStream<U> ofType(Class<U> type){
		return (FutureStream<U>)ofType(this,type);
	}

	 /**
     * Keep only those elements in a stream that are of a given type.
     * 
     * 
     * // (1, 2, 3)
     * EagerFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
     * 
     */
    @SuppressWarnings("unchecked")
    static <T, U> FutureStream<U> ofType(FutureStream<T> stream, Class<U> type) {
        return stream.filter(type::isInstance).map(t -> (U) t);
    }
    /**
     * Cast all elements in a stream to a given type, possibly throwing a {@link ClassCastException}.
     * 
     * 
     * // ClassCastException
     * LazyFutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
     * 
     */
	static <T, U> FutureStream<U> cast(FutureStream<T> stream, Class<U> type) {
        return stream.map(type::cast);
    }
	
	
	 /**
     * Returns a stream with a given value interspersed between any two values of this stream.
     * 
     * 
     * // (1, 0, 2, 0, 3, 0, 4)
     * Seq.of(1, 2, 3, 4).intersperse(0)
     * 
     */
    static <T> FutureStream<T> intersperse(FutureStream<T> stream, T value) {
        return stream.flatMap(t -> Stream.of(value, t).skip(1));
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
		return toQueue().stream(getSubscription()).mapToInt(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#mapToLong(java.util.function.ToLongFunction)
	 */
	@Override
	default LongStream mapToLong(ToLongFunction<? super U> mapper) {
		return toQueue().stream(getSubscription()).mapToLong(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#mapToDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	default DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) {
		return toQueue().stream(getSubscription()).mapToDouble(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#flatMapToInt(java.util.function.Function)
	 */
	@Override
	default IntStream flatMapToInt(
			Function<? super U, ? extends IntStream> mapper) {
		return toQueue().stream(getSubscription()).flatMapToInt(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#flatMapToLong(java.util.function.Function)
	 */
	@Override
	default LongStream flatMapToLong(
			Function<? super U, ? extends LongStream> mapper) {
		return toQueue().stream(getSubscription()).flatMapToLong(mapper);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#flatMapToDouble(java.util.function.Function)
	 */
	@Override
	default DoubleStream flatMapToDouble(
			Function<? super U, ? extends DoubleStream> mapper) {
		return toQueue().stream(getSubscription()).flatMapToDouble(mapper);
	}

	

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sorted()
	 */
	@Override
	default Seq<U> sorted() {
		return toQueue().stream(getSubscription()).sorted();
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	default Seq<U> sorted(Comparator<? super U> comparator) {
		return toQueue().stream(getSubscription()).sorted(comparator);
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
     * 
     * 
     * // (4, 5)
     * Seq.of(1, 2, 3, 4, 5, 6).slice(3, 5)
     * 
     *
     * @see #slice(Stream, long, long)
     */
    @Override
    default Seq<U> slice(long from, long to) {
        return Seq.super.slice(from, to);
    }
    
  
    /**
     * Returns a limited interval from a given Stream.
     * 
     * 
     * // (4, 5)
     * EagerFutureStream.of(1, 2, 3, 4, 5, 6).slice(3, 5)
     * 
     */
    static <T> Seq<T> slice(FutureStream<T> stream, long from, long to) {
        long f = Math.max(from, 0);
        long t = Math.max(to - f, 0);

        return stream.skip(f).limit(t);
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
