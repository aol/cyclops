package com.aol.cyclops.internal.stream;

import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.types.stream.future.DoubleOperatorsMixin;
import com.aol.cyclops.types.stream.future.FutureOperations;
import com.aol.cyclops.types.stream.future.IntOperatorsMixin;
import com.aol.cyclops.types.stream.future.LongOperatorsMixin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class BaseFutureOperationsImpl<T>
        implements DoubleOperatorsMixin<T>, IntOperatorsMixin<T>, LongOperatorsMixin<T>, FutureOperations<T> {

    @Getter
    private final Executor exec;
    @Getter
    private final ReactiveSeq<T> stream;

    /**
     * Asynchronously convert  this Stream into a List
     *  <pre>
     * {@code
     *  CompletableFuture<List<Data>> myList = EagerFutureStream.of(1,2,3,4)
     *  														.map(this::loadFromDb)
     *  														.withTaskExecutor(parallelBuilder().getExecutor())
     *  														.map(this::processOnDifferentExecutor)
     *  														.toList();
     * }
     * </pre>
     * 
     * @return Future List
     */
    @Override
    public CompletableFuture<List<T>> toList() {
        return CompletableFuture.supplyAsync(() -> stream.toList(), exec);
    }

    /**
     * @return Last value in this Stream (must be non-empty)
     */
    @Override
    public CompletableFuture<T> lastValue() {
        return CompletableFuture.supplyAsync(() -> {
            final List<T> l = stream.toList();
            return l.get(l.size() - 1);
        } , exec);
    }

    /**
     * @return the only entry in this Stream if it is a single entry Stream,
     *         otherwise throws an UnsupportedOperationException
     */
    @Override
    public CompletableFuture<T> single() {
        return CompletableFuture.supplyAsync(() -> {
            final List<T> l = stream.toList();
            if (l.size() == 1) {
                return l.get(l.size() - 1);
            }
            throw new UnsupportedOperationException(
                                                    "single only works for Streams with a single value");
        } , exec);
    }

    /**
     * Asynchronously convert  this Stream into a List
     *  <pre>
     * {@code
     *  CompletableFuture<Set<Data>> myList = LazyFutureStream.of(1,2,3,4)
     *  														.map(this::loadFromDb)
     *  														.withTaskExecutor(parallelBuilder().getExecutor())
     *  														.map(this::processOnDifferentExecutor)
     *  														.toSet();
     * }
     * </pre>
     * 
     * @return Future Set
     */
    @Override
    public CompletableFuture<Set<T>> toSet() {
        return CompletableFuture.supplyAsync(() -> stream.toSet(), exec);
    }

    /**
     * Asynchronously capture the minimum value in this stream using the provided function
     * 
     * @see org.jooq.lambda.Seq#minBy(Function)
     */
    @Override
    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> minBy(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.minBy(function));
    }

    /**
     * Asynchronously capture the maximum value in this stream using the provided function
     * 
     *  @see org.jooq.lambda.Seq#maxBy(Function)
     */
    @Override
    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> maxBy(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.maxBy(function));
    }

    /**
     * Asynchronously perform a Stream collection
     * 
     * @see java.util.stream.Stream#collect(Collector)
     * 
     */
    @Override
    public <R, A> CompletableFuture<R> collect(final Collector<? super T, A, R> collector) {
        return CompletableFuture.supplyAsync(() -> stream.collect(collector));
    }

    /**
     *  Asynchronously perform a Stream collection
     * @see org.jooq.lambda.Seq#toCollection(Supplier)
     */
    @Override
    public <C extends Collection<T>> CompletableFuture<C> toCollection(final Supplier<C> collectionFactory) {
        return CompletableFuture.supplyAsync(() -> stream.toCollection(collectionFactory), exec);
    }

    /**
     * Asyncrhonously generate an Array
     * 
     * @see java.util.stream.Stream#toArray(IntFunction)
     */
    @Override
    public <A> CompletableFuture<A[]> toArray(final IntFunction<A[]> generator) {
        return CompletableFuture.supplyAsync(() -> stream.toArray(generator), exec);
    }

    /**
     * Asyncrhonously generate an Array
     * 
     * @see java.util.stream.Stream#toArray(IntFunction)
     */
    @Override
    public CompletableFuture<Object[]> toArray() {
        return CompletableFuture.supplyAsync(() -> stream.toArray(), exec);
    }

    /**
     * Perform an asyncrhonous groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function)
     */
    @Override
    public <K> CompletableFuture<Map<K, List<T>>> groupBy(final Function<? super T, ? extends K> classifier) {
        return CompletableFuture.supplyAsync(() -> stream.groupBy(classifier), exec);
    }

    /**
     * Perform an asyncrhonous groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function, Collector)
     */
    @Override
    public <K, A, D> CompletableFuture<Map<K, D>> groupBy(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return CompletableFuture.supplyAsync(() -> stream.groupBy(classifier, downstream), exec);
    }

    /**
     * Perform an asyncrhonous groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function, Supplier, Collector)
     */
    @Override
    public <K, D, A, M extends Map<K, D>> CompletableFuture<M> groupBy(final Function<? super T, ? extends K> classifier,
            final Supplier<M> mapFactory, final Collector<? super T, A, D> downstream) {
        return CompletableFuture.supplyAsync(() -> stream.groupBy(classifier, mapFactory, downstream), exec);
    }

    /**
     * Perform an asynchronous foldLeft operation
     * 	 @see org.jooq.lambda.Seq#foldLeft(Object,BiFunction)
     * */
    @Override
    public <U> CompletableFuture<U> foldLeft(final U seed, final BiFunction<U, ? super T, U> function) {
        return CompletableFuture.supplyAsync(() -> stream.foldLeft(seed, function), exec);
    }

    /**
     * Perform an asynchronous foldRight operation
     * 	 @see org.jooq.lambda.Seq#foldRight(Object,BiFunction)
     * */
    @Override
    public <U> CompletableFuture<U> foldRight(final U seed, final BiFunction<? super T, U, U> function) {
        return CompletableFuture.supplyAsync(() -> stream.foldRight(seed, function), exec);
    }

    /**
     * Perform an asyncrhonous min operation
     *  @see java.util.stream.Stream#min(Comparator)
     */
    @Override
    public CompletableFuture<Optional<T>> min(final Comparator<? super T> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.min(comparator), exec);
    }

    /**
     * Perform an asyncrhonous min operation
     *  @see java.util.stream.Stream#max(Comparator)
     */
    @Override
    public CompletableFuture<Optional<T>> max(final Comparator<? super T> comparator) {

        return CompletableFuture.supplyAsync(() -> stream.max(comparator), exec);
    }

    /**
     * Asynchronously perform a Stream collection
     * 
     * @see java.util.stream.Stream#collect(Supplier, BiConsumer, BiConsumer)
     * 
     */
    @Override
    public <R> CompletableFuture<R> collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return CompletableFuture.supplyAsync(() -> stream.collect(supplier, accumulator, combiner), exec);
    }

    /**
     * Asynchronously perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)
     * 
     */
    @Override
    public <U> CompletableFuture<U> reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return CompletableFuture.supplyAsync(() -> stream.reduce(identity, accumulator, combiner), exec);
    }

    /**
     * Asynchronously perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(BinaryOperator)
     * 
     */
    @Override
    public CompletableFuture<Optional<T>> reduce(final BinaryOperator<T> accumulator) {

        return CompletableFuture.supplyAsync(() -> stream.reduce(accumulator));

    }

    /**
     * Asynchronously perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(Object, BinaryOperator)
     * 
     */
    @Override
    public CompletableFuture<T> reduce(final T identity, final BinaryOperator<T> accumulator) {
        return CompletableFuture.supplyAsync(() -> stream.reduce(identity, accumulator), exec);
    }

    /**
     * Asynchronously perform a Stream count
     * 
     * @see java.util.stream.Stream#count()
     * 
     */
    @Override
    public CompletableFuture<Long> count() {
        return CompletableFuture.supplyAsync(() -> stream.count(), exec);
    }

    @Override
    public CompletableFuture<String> join(final CharSequence sep) {
        return CompletableFuture.supplyAsync(() -> stream.join(sep), exec);
    }

    /**
     * Perform an asynchronous join operation
     * 	 @see org.jooq.lambda.Seq#join()
     * */
    @Override
    public CompletableFuture<String> join() {
        return CompletableFuture.supplyAsync(() -> stream.join(), exec);
    }

    /**
     * Perform an asynchronous join operation
     * 	 @see org.jooq.lambda.Seq#join(CharSequence)
     * */
    @Override
    public CompletableFuture<String> join(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return CompletableFuture.supplyAsync(() -> stream.join(delimiter, prefix, suffix), exec);
    }

    /**
     * Perform an asynchronous findAny operation
     * 	 @see java.util.stream.Stream#findAny()
     * */
    @Override
    public CompletableFuture<Optional<T>> findAny() {
        return CompletableFuture.supplyAsync(() -> stream.findAny(), exec);
    }

    /**
     * Perform an asynchronous findAny operation
     * 	 @see java.util.stream.Stream#findFirst()
     * */
    @Override
    public CompletableFuture<Optional<T>> findFirst() {
        return CompletableFuture.supplyAsync(() -> stream.findFirst(), exec);
    }

    /**
     * Perform an asynchronous findAny operation
     * 	 @see java.util.stream.Stream#findFirst()
     * */
    @Override
    public CompletableFuture<T> firstValue() {
        return CompletableFuture.supplyAsync(() -> stream.firstValue(), exec);
    }

    /**
     * Perform an asynchronous All Match operation
     * 	@see java.util.stream.Stream#allMatch(Predicate)
     * */
    @Override
    public CompletableFuture<Boolean> allMatch(final Predicate<? super T> predicate) {
        return CompletableFuture.supplyAsync(() -> stream.allMatch(predicate), exec);
    }

    /**
     * Perform an asynchronous Any Match operation
     * 	 @see java.util.stream.Stream#anyMatch(Predicate)
     * */
    @Override
    public CompletableFuture<Boolean> anyMatch(final Predicate<? super T> predicate) {
        return CompletableFuture.supplyAsync(() -> stream.anyMatch(predicate), exec);
    }

    /**
     * Perform an asynchronous Any Match operation
     * 	 @see java.util.stream.Stream#noneMatch(Predicate)
     * */
    @Override
    public CompletableFuture<Boolean> noneMatch(final Predicate<? super T> predicate) {
        return CompletableFuture.supplyAsync(() -> stream.noneMatch(predicate), exec);
    }

    @Override
    public void forEach(final Consumer<? super T> c) {
        CompletableFuture.runAsync(() -> stream.forEach(c), exec);

    }

    @Override
    public CompletableFuture<T> single(final Predicate<T> predicate) {
        return CompletableFuture.supplyAsync(() -> stream.filter(predicate)
                                                         .single(),
                                             exec);

    }

    @Override
    public CompletableFuture<Optional<T>> singleOptional() {
        return CompletableFuture.supplyAsync(() -> stream.singleOptional(), exec);
    }

    @Override
    public CompletableFuture<Long> countDistinct() {
        return CompletableFuture.supplyAsync(() -> stream.countDistinct(), exec);
    }

    @Override
    public <U> CompletableFuture<Long> countDistinctBy(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.countDistinctBy(function), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> mode() {
        return CompletableFuture.supplyAsync(() -> stream.mode(), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> sum() {
        return CompletableFuture.supplyAsync(() -> stream.sum(), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<U>> sum(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.sum(function), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> avg() {
        return CompletableFuture.supplyAsync(() -> stream.avg(), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<U>> avg(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.avg(function), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> min() {
        return CompletableFuture.supplyAsync(() -> stream.min(), exec);
    }

    @Override
    public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> min(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.min(function), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<U>> min(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.min(function, comparator), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<T>> minBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.minBy(function, comparator), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> max() {
        return CompletableFuture.supplyAsync(() -> stream.max(), exec);
    }

    @Override
    public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> max(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.max(function), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<U>> max(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.max(function, comparator), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<T>> maxBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.maxBy(function, comparator), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> median() {
        return CompletableFuture.supplyAsync(() -> stream.median(), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> median(final Comparator<? super T> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.median(comparator), exec);
    }

    @Override
    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> medianBy(final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.medianBy(function), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<T>> medianBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.medianBy(function, comparator), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> percentile(final double percentile) {
        return CompletableFuture.supplyAsync(() -> stream.percentile(percentile), exec);
    }

    @Override
    public CompletableFuture<Optional<T>> percentile(final double percentile, final Comparator<? super T> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.percentile(percentile, comparator), exec);
    }

    @Override
    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> percentileBy(final double percentile,
            final Function<? super T, ? extends U> function) {
        return CompletableFuture.supplyAsync(() -> stream.percentileBy(percentile, function), exec);
    }

    @Override
    public <U> CompletableFuture<Optional<T>> percentileBy(final double percentile, final Function<? super T, ? extends U> function,
            final Comparator<? super U> comparator) {
        return CompletableFuture.supplyAsync(() -> stream.percentileBy(percentile, function, comparator), exec);
    }

    @Override
    public <L extends List<T>> CompletableFuture<L> toList(final Supplier<L> factory) {
        return CompletableFuture.supplyAsync(() -> stream.toList(factory), exec);
    }

    @Override
    public <S extends Set<T>> CompletableFuture<S> toSet(final Supplier<S> factory) {
        return CompletableFuture.supplyAsync(() -> stream.toSet(factory), exec);
    }

    @Override
    public <K, V> CompletableFuture<MapX<K, V>> toMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends V> valueMapper) {
        return CompletableFuture.supplyAsync(() -> stream.toMapX(keyMapper, valueMapper), exec);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#sumDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public CompletableFuture<Double> sumDouble(final ToDoubleFunction<? super T> fn) {
        return DoubleOperatorsMixin.super.sumDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#sumLong(java.util.function.ToLongFunction)
     */
    @Override
    public CompletableFuture<Long> sumLong(final ToLongFunction<? super T> fn) {

        return LongOperatorsMixin.super.sumLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#maxLong(java.util.function.ToLongFunction)
     */
    @Override
    public CompletableFuture<OptionalLong> maxLong(final ToLongFunction<? super T> fn) {

        return LongOperatorsMixin.super.maxLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#minLong(java.util.function.ToLongFunction)
     */
    @Override
    public CompletableFuture<OptionalLong> minLong(final ToLongFunction<? super T> fn) {

        return LongOperatorsMixin.super.minLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#averageLong(java.util.function.ToLongFunction)
     */
    @Override
    public CompletableFuture<OptionalDouble> averageLong(final ToLongFunction<? super T> fn) {

        return LongOperatorsMixin.super.averageLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#summaryStatisticsLong(java.util.function.ToLongFunction)
     */
    @Override
    public CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(final ToLongFunction<? super T> fn) {

        return LongOperatorsMixin.super.summaryStatisticsLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#sumInt(java.util.function.ToIntFunction)
     */
    @Override
    public CompletableFuture<Integer> sumInt(final ToIntFunction<? super T> fn) {

        return IntOperatorsMixin.super.sumInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#maxInt(java.util.function.ToIntFunction)
     */
    @Override
    public CompletableFuture<OptionalInt> maxInt(final ToIntFunction<? super T> fn) {

        return IntOperatorsMixin.super.maxInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#minInt(java.util.function.ToIntFunction)
     */
    @Override
    public CompletableFuture<OptionalInt> minInt(final ToIntFunction<? super T> fn) {

        return IntOperatorsMixin.super.minInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#averageInt(java.util.function.ToIntFunction)
     */
    @Override
    public CompletableFuture<OptionalDouble> averageInt(final ToIntFunction<? super T> fn) {

        return IntOperatorsMixin.super.averageInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#summaryStatisticsInt(java.util.function.ToIntFunction)
     */
    @Override
    public CompletableFuture<IntSummaryStatistics> summaryStatisticsInt(final ToIntFunction<? super T> fn) {

        return IntOperatorsMixin.super.summaryStatisticsInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#maxDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public CompletableFuture<OptionalDouble> maxDouble(final ToDoubleFunction<? super T> fn) {

        return DoubleOperatorsMixin.super.maxDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#minDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public CompletableFuture<OptionalDouble> minDouble(final ToDoubleFunction<? super T> fn) {

        return DoubleOperatorsMixin.super.minDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#averageDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public CompletableFuture<OptionalDouble> averageDouble(final ToDoubleFunction<? super T> fn) {

        return DoubleOperatorsMixin.super.averageDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#summaryStatisticsDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public CompletableFuture<DoubleSummaryStatistics> summaryStatisticsDouble(final ToDoubleFunction<? super T> fn) {

        return DoubleOperatorsMixin.super.summaryStatisticsDouble(fn);
    }

}