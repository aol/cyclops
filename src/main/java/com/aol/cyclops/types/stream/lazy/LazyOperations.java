package com.aol.cyclops.types.stream.lazy;

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

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.MapX;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LazyOperations<T> implements DoubleOperatorsMixin<T>, IntOperatorsMixin<T>, LongOperatorsMixin<T>, LazyCollectable<T> {

    @Getter
    private final ReactiveSeq<T> stream;

    /**
     * Asynchronously convert  this Stream into a List
     *  <pre>
     * {@code
     *  Eval<List<Data>> myList = EagerFutureStream.of(1,2,3,4)
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
    public Eval<List<T>> toList() {
        return Eval.later(() -> stream.toList());
    }

    /**
     * @return Last value in this Stream (must be non-empty)
     */
    @Override
    public Eval<T> lastValue() {
        return Eval.later(() -> {
            final List<T> l = stream.toList();
            return l.get(l.size() - 1);
        });
    }

    /**
     * @return the only entry in this Stream if it is a single entry Stream,
     *         otherwise throws an UnsupportedOperationException
     */
    @Override
    public Eval<T> single() {
        return Eval.later(() -> {
            final List<T> l = stream.toList();
            if (l.size() == 1) {
                return l.get(l.size() - 1);
            }
            throw new UnsupportedOperationException(
                                                    "single only works for Streams with a single value");
        });
    }

    /**
     * Asynchronously convert  this Stream into a List
     *  <pre>
     * {@code
     *  Eval<Set<Data>> myList = LazyFutureStream.of(1,2,3,4)
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
    public Eval<Set<T>> toSet() {
        return Eval.later(() -> stream.toSet());
    }

    /**
     * Asynchronously capture the minimum value in this stream using the provided function
     * 
     * @see org.jooq.lambda.Seq#minBy(Function)
     */
    @Override
    public <U extends Comparable<? super U>> Eval<Optional<T>> minBy(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.minBy(function));
    }

    /**
     * Asynchronously capture the maximum value in this stream using the provided function
     * 
     *  @see org.jooq.lambda.Seq#maxBy(Function)
     */
    @Override
    public <U extends Comparable<? super U>> Eval<Optional<T>> maxBy(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.maxBy(function));
    }

    /**
     * Asynchronously perform a Stream collection
     * 
     * @see java.util.stream.Stream#collect(Collector)
     * 
     */
    @Override
    public <R, A> Eval<R> collect(final Collector<? super T, A, R> collector) {
        return Eval.later(() -> stream.collect(collector));
    }

    /**
     *  Asynchronously perform a Stream collection
     * @see org.jooq.lambda.Seq#toCollection(Supplier)
     */
    @Override
    public <C extends Collection<T>> Eval<C> toCollection(final Supplier<C> collectionFactory) {
        return Eval.later(() -> stream.toCollection(collectionFactory));
    }

    /**
     * Asyncrhonously generate an Array
     * 
     * @see java.util.stream.Stream#toArray(IntFunction)
     */
    @Override
    public <A> Eval<A[]> toArray(final IntFunction<A[]> generator) {
        return Eval.later(() -> stream.toArray(generator));
    }

    /**
     * Asyncrhonously generate an Array
     * 
     * @see java.util.stream.Stream#toArray(IntFunction)
     */
    @Override
    public Eval<Object[]> toArray() {
        return Eval.later(() -> stream.toArray());
    }

    /**
     * Perform an asyncrhonous groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function)
     */
    @Override
    public <K> Eval<Map<K, List<T>>> groupBy(final Function<? super T, ? extends K> classifier) {
        return Eval.later(() -> stream.groupBy(classifier));
    }

    /**
     * Perform an asyncrhonous groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function, Collector)
     */
    @Override
    public <K, A, D> Eval<Map<K, D>> groupBy(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return Eval.later(() -> stream.groupBy(classifier, downstream));
    }

    /**
     * Perform an asyncrhonous groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function, Supplier, Collector)
     */
    @Override
    public <K, D, A, M extends Map<K, D>> Eval<M> groupBy(final Function<? super T, ? extends K> classifier, final Supplier<M> mapFactory,
            final Collector<? super T, A, D> downstream) {
        return Eval.later(() -> stream.groupBy(classifier, mapFactory, downstream));
    }

    /**
     * Perform an asynchronous foldLeft operation
     * 	 @see org.jooq.lambda.Seq#foldLeft(Object,BiFunction)
     * */
    @Override
    public <U> Eval<U> foldLeft(final U seed, final BiFunction<U, ? super T, U> function) {
        return Eval.later(() -> stream.foldLeft(seed, function));
    }

    /**
     * Perform an asynchronous foldRight operation
     * 	 @see org.jooq.lambda.Seq#foldRight(Object,BiFunction)
     * */
    @Override
    public <U> Eval<U> foldRight(final U seed, final BiFunction<? super T, U, U> function) {
        return Eval.later(() -> stream.foldRight(seed, function));
    }

    /**
     * Perform an asyncrhonous min operation
     *  @see java.util.stream.Stream#min(Comparator)
     */
    @Override
    public Eval<Optional<T>> min(final Comparator<? super T> comparator) {
        return Eval.later(() -> stream.min(comparator));
    }

    /**
     * Perform an asyncrhonous min operation
     *  @see java.util.stream.Stream#max(Comparator)
     */
    @Override
    public Eval<Optional<T>> max(final Comparator<? super T> comparator) {

        return Eval.later(() -> stream.max(comparator));
    }

    /**
     * Asynchronously perform a Stream collection
     * 
     * @see java.util.stream.Stream#collect(Supplier, BiConsumer, BiConsumer)
     * 
     */
    @Override
    public <R> Eval<R> collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return Eval.later(() -> stream.collect(supplier, accumulator, combiner));
    }

    /**
     * Asynchronously perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)
     * 
     */
    @Override
    public <U> Eval<U> reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return Eval.later(() -> stream.reduce(identity, accumulator, combiner));
    }

    /**
     * Asynchronously perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(BinaryOperator)
     * 
     */
    @Override
    public Eval<Optional<T>> reduce(final BinaryOperator<T> accumulator) {

        return Eval.later(() -> stream.reduce(accumulator));

    }

    /**
     * Asynchronously perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(Object, BinaryOperator)
     * 
     */
    @Override
    public Eval<T> reduce(final T identity, final BinaryOperator<T> accumulator) {
        return Eval.later(() -> stream.reduce(identity, accumulator));
    }

    /**
     * Asynchronously perform a Stream count
     * 
     * @see java.util.stream.Stream#count()
     * 
     */
    @Override
    public Eval<Long> count() {
        return Eval.later(() -> stream.count());
    }

    @Override
    public Eval<String> join(final CharSequence sep) {
        return Eval.later(() -> stream.join(sep));
    }

    /**
     * Perform an asynchronous join operation
     * 	 @see org.jooq.lambda.Seq#join()
     * */
    @Override
    public Eval<String> join() {
        return Eval.later(() -> stream.join());
    }

    /**
     * Perform an asynchronous join operation
     * 	 @see org.jooq.lambda.Seq#join(CharSequence)
     * */
    @Override
    public Eval<String> join(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return Eval.later(() -> stream.join(delimiter, prefix, suffix));
    }

    /**
     * Perform an asynchronous findAny operation
     * 	 @see java.util.stream.Stream#findAny()
     * */
    @Override
    public Eval<Optional<T>> findAny() {
        return Eval.later(() -> stream.findAny());
    }

    /**
     * Perform an asynchronous findAny operation
     * 	 @see java.util.stream.Stream#findFirst()
     * */
    @Override
    public Eval<Optional<T>> findFirst() {
        return Eval.later(() -> stream.findFirst());
    }

    /**
     * Perform an asynchronous findAny operation
     * 	 @see java.util.stream.Stream#findFirst()
     * */
    @Override
    public Eval<T> firstValue() {
        return Eval.later(() -> stream.firstValue());
    }

    /**
     * Perform an asynchronous All Match operation
     * 	@see java.util.stream.Stream#allMatch(Predicate)
     * */
    @Override
    public Eval<Boolean> allMatch(final Predicate<? super T> predicate) {
        return Eval.later(() -> stream.allMatch(predicate));
    }

    /**
     * Perform an asynchronous Any Match operation
     * 	 @see java.util.stream.Stream#anyMatch(Predicate)
     * */
    @Override
    public Eval<Boolean> anyMatch(final Predicate<? super T> predicate) {
        return Eval.later(() -> stream.anyMatch(predicate));
    }

    /**
     * Perform an asynchronous Any Match operation
     * 	 @see java.util.stream.Stream#noneMatch(Predicate)
     * */
    @Override
    public Eval<Boolean> noneMatch(final Predicate<? super T> predicate) {
        return Eval.later(() -> stream.noneMatch(predicate));
    }

    public Eval<Void> forEach(final Consumer<T> c) {
        return Eval.later(() -> {
            stream.forEach(c);
            return null;
        });

    }

    @Override
    public Eval<T> single(final Predicate<T> predicate) {
        return Eval.later(() -> stream.filter(predicate)
                                      .single());

    }

    @Override
    public Eval<Optional<T>> singleOptional() {
        return Eval.later(() -> stream.singleOptional());
    }

    @Override
    public Eval<Long> countDistinct() {
        return Eval.later(() -> stream.countDistinct());
    }

    @Override
    public <U> Eval<Long> countDistinctBy(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.countDistinctBy(function));
    }

    @Override
    public Eval<Optional<T>> mode() {
        return Eval.later(() -> stream.mode());
    }

    @Override
    public Eval<Optional<T>> sum() {
        return Eval.later(() -> stream.sum());
    }

    @Override
    public <U> Eval<Optional<U>> sum(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.sum(function));
    }

    @Override
    public Eval<Optional<T>> avg() {
        return Eval.later(() -> stream.avg());
    }

    @Override
    public <U> Eval<Optional<U>> avg(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.avg(function));
    }

    @Override
    public Eval<Optional<T>> min() {
        return Eval.later(() -> stream.min());
    }

    @Override
    public <U extends Comparable<? super U>> Eval<Optional<U>> min(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.min(function));
    }

    @Override
    public <U> Eval<Optional<U>> min(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return Eval.later(() -> stream.min(function, comparator));
    }

    @Override
    public <U> Eval<Optional<T>> minBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return Eval.later(() -> stream.minBy(function, comparator));
    }

    @Override
    public Eval<Optional<T>> max() {
        return Eval.later(() -> stream.max());
    }

    @Override
    public <U extends Comparable<? super U>> Eval<Optional<U>> max(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.max(function));
    }

    @Override
    public <U> Eval<Optional<U>> max(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return Eval.later(() -> stream.max(function, comparator));
    }

    @Override
    public <U> Eval<Optional<T>> maxBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return Eval.later(() -> stream.maxBy(function, comparator));
    }

    @Override
    public Eval<Optional<T>> median() {
        return Eval.later(() -> stream.median());
    }

    @Override
    public Eval<Optional<T>> median(final Comparator<? super T> comparator) {
        return Eval.later(() -> stream.median(comparator));
    }

    @Override
    public <U extends Comparable<? super U>> Eval<Optional<T>> medianBy(final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.medianBy(function));
    }

    @Override
    public <U> Eval<Optional<T>> medianBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return Eval.later(() -> stream.medianBy(function, comparator));
    }

    @Override
    public Eval<Optional<T>> percentile(final double percentile) {
        return Eval.later(() -> stream.percentile(percentile));
    }

    @Override
    public Eval<Optional<T>> percentile(final double percentile, final Comparator<? super T> comparator) {
        return Eval.later(() -> stream.percentile(percentile, comparator));
    }

    @Override
    public <U extends Comparable<? super U>> Eval<Optional<T>> percentileBy(final double percentile,
            final Function<? super T, ? extends U> function) {
        return Eval.later(() -> stream.percentileBy(percentile, function));
    }

    @Override
    public <U> Eval<Optional<T>> percentileBy(final double percentile, final Function<? super T, ? extends U> function,
            final Comparator<? super U> comparator) {
        return Eval.later(() -> stream.percentileBy(percentile, function, comparator));
    }

    @Override
    public <L extends List<T>> Eval<L> toList(final Supplier<L> factory) {
        return Eval.later(() -> stream.toList(factory));
    }

    @Override
    public <S extends Set<T>> Eval<S> toSet(final Supplier<S> factory) {
        return Eval.later(() -> stream.toSet(factory));
    }

    @Override
    public <K, V> Eval<MapX<K, V>> toMap(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return Eval.later(() -> stream.toMapX(keyMapper, valueMapper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#sumDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public Eval<Double> sumDouble(final ToDoubleFunction<? super T> fn) {
        return DoubleOperatorsMixin.super.sumDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#sumLong(java.util.function.ToLongFunction)
     */
    @Override
    public Eval<Long> sumLong(final ToLongFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return LongOperatorsMixin.super.sumLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#maxLong(java.util.function.ToLongFunction)
     */
    @Override
    public Eval<OptionalLong> maxLong(final ToLongFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return LongOperatorsMixin.super.maxLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#minLong(java.util.function.ToLongFunction)
     */
    @Override
    public Eval<OptionalLong> minLong(final ToLongFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return LongOperatorsMixin.super.minLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#averageLong(java.util.function.ToLongFunction)
     */
    @Override
    public Eval<OptionalDouble> averageLong(final ToLongFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return LongOperatorsMixin.super.averageLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.LongOperatorsMixin#summaryStatisticsLong(java.util.function.ToLongFunction)
     */
    @Override
    public Eval<LongSummaryStatistics> summaryStatisticsLong(final ToLongFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return LongOperatorsMixin.super.summaryStatisticsLong(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#sumInt(java.util.function.ToIntFunction)
     */
    @Override
    public Eval<Integer> sumInt(final ToIntFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return IntOperatorsMixin.super.sumInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#maxInt(java.util.function.ToIntFunction)
     */
    @Override
    public Eval<OptionalInt> maxInt(final ToIntFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return IntOperatorsMixin.super.maxInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#minInt(java.util.function.ToIntFunction)
     */
    @Override
    public Eval<OptionalInt> minInt(final ToIntFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return IntOperatorsMixin.super.minInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#averageInt(java.util.function.ToIntFunction)
     */
    @Override
    public Eval<OptionalDouble> averageInt(final ToIntFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return IntOperatorsMixin.super.averageInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.IntOperatorsMixin#summaryStatisticsInt(java.util.function.ToIntFunction)
     */
    @Override
    public Eval<IntSummaryStatistics> summaryStatisticsInt(final ToIntFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return IntOperatorsMixin.super.summaryStatisticsInt(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#maxDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public Eval<OptionalDouble> maxDouble(final ToDoubleFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return DoubleOperatorsMixin.super.maxDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#minDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public Eval<OptionalDouble> minDouble(final ToDoubleFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return DoubleOperatorsMixin.super.minDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#averageDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public Eval<OptionalDouble> averageDouble(final ToDoubleFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return DoubleOperatorsMixin.super.averageDouble(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#summaryStatisticsDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    public Eval<DoubleSummaryStatistics> summaryStatisticsDouble(final ToDoubleFunction<? super T> fn) {
        // TODO Auto-generated method stub
        return DoubleOperatorsMixin.super.summaryStatisticsDouble(fn);
    }

}