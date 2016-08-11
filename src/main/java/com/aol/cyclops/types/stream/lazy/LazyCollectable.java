package com.aol.cyclops.types.stream.lazy;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.data.collections.extensions.standard.MapX;

public interface LazyCollectable<T> {

    /**
    * Collect this collectable into 2 {@link Collector}s.
    */
    default <R1, R2, A1, A2> Eval<Tuple2<R1, R2>> collect(Collector<? super T, A1, R1> collector1, Collector<? super T, A2, R2> collector2) {
        return collect(Tuple.collectors(collector1, collector2));
    }

    /**
     * Collect this collectable into 3 {@link Collector}s.
     */
    default <R1, R2, R3, A1, A2, A3> Eval<Tuple3<R1, R2, R3>> collect(Collector<? super T, A1, R1> collector1,
            Collector<? super T, A2, R2> collector2, Collector<? super T, A3, R3> collector3) {
        return collect(Tuple.collectors(collector1, collector2, collector3));
    }

    /**
     * Collect this collectable into 4 {@link Collector}s.
     */
    default <R1, R2, R3, R4, A1, A2, A3, A4> Eval<Tuple4<R1, R2, R3, R4>> collect(Collector<? super T, A1, R1> collector1,
            Collector<? super T, A2, R2> collector2, Collector<? super T, A3, R3> collector3, Collector<? super T, A4, R4> collector4) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4));
    }

    /**
     * Collect this collectable into 5 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, A1, A2, A3, A4, A5> Eval<Tuple5<R1, R2, R3, R4, R5>> collect(Collector<? super T, A1, R1> collector1,
            Collector<? super T, A2, R2> collector2, Collector<? super T, A3, R3> collector3, Collector<? super T, A4, R4> collector4,
            Collector<? super T, A5, R5> collector5) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5));
    }

    /**
     * Collect this collectable into 6 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, A1, A2, A3, A4, A5, A6> Eval<Tuple6<R1, R2, R3, R4, R5, R6>> collect(Collector<? super T, A1, R1> collector1,
            Collector<? super T, A2, R2> collector2, Collector<? super T, A3, R3> collector3, Collector<? super T, A4, R4> collector4,
            Collector<? super T, A5, R5> collector5, Collector<? super T, A6, R6> collector6) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6));
    }

    /**
     * Collect this collectable into 7 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, R7, A1, A2, A3, A4, A5, A6, A7> Eval<Tuple7<R1, R2, R3, R4, R5, R6, R7>> collect(
            Collector<? super T, A1, R1> collector1, Collector<? super T, A2, R2> collector2, Collector<? super T, A3, R3> collector3,
            Collector<? super T, A4, R4> collector4, Collector<? super T, A5, R5> collector5, Collector<? super T, A6, R6> collector6,
            Collector<? super T, A7, R7> collector7) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6, collector7));
    }

    /**
     * Collect this collectable into 8 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, R7, R8, A1, A2, A3, A4, A5, A6, A7, A8> Eval<Tuple8<R1, R2, R3, R4, R5, R6, R7, R8>> collect(
            Collector<? super T, A1, R1> collector1, Collector<? super T, A2, R2> collector2, Collector<? super T, A3, R3> collector3,
            Collector<? super T, A4, R4> collector4, Collector<? super T, A5, R5> collector5, Collector<? super T, A6, R6> collector6,
            Collector<? super T, A7, R7> collector7, Collector<? super T, A8, R8> collector8) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6, collector7, collector8));
    }

    public Eval<Long> countDistinct();

    public <U> Eval<Long> countDistinctBy(Function<? super T, ? extends U> function);

    public Eval<Optional<T>> mode();

    public Eval<Optional<T>> sum();

    public <U> Eval<Optional<U>> sum(Function<? super T, ? extends U> function);

    public Eval<Integer> sumInt(ToIntFunction<? super T> function);

    public Eval<Long> sumLong(ToLongFunction<? super T> function);

    public Eval<Double> sumDouble(ToDoubleFunction<? super T> function);

    public Eval<Optional<T>> avg();

    public <U> Eval<Optional<U>> avg(Function<? super T, ? extends U> function);

    public Eval<Optional<T>> min();

    public <U extends Comparable<? super U>> Eval<Optional<U>> min(Function<? super T, ? extends U> function);

    public <U> Eval<Optional<U>> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public <U> Eval<Optional<T>> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public Eval<Optional<T>> max();

    public <U extends Comparable<? super U>> Eval<Optional<U>> max(Function<? super T, ? extends U> function);

    public <U> Eval<Optional<U>> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public <U> Eval<Optional<T>> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public Eval<Optional<T>> median();

    public Eval<Optional<T>> median(Comparator<? super T> comparator);

    public <U extends Comparable<? super U>> Eval<Optional<T>> medianBy(Function<? super T, ? extends U> function);

    public <U> Eval<Optional<T>> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public Eval<Optional<T>> percentile(double percentile);

    public Eval<Optional<T>> percentile(double percentile, Comparator<? super T> comparator);

    public <U extends Comparable<? super U>> Eval<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function);

    public <U> Eval<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public Eval<Boolean> allMatch(Predicate<? super T> predicate);

    public Eval<Boolean> anyMatch(Predicate<? super T> predicate);

    public Eval<Boolean> noneMatch(Predicate<? super T> predicate);

    public <L extends List<T>> Eval<L> toList(Supplier<L> factory);

    public <S extends Set<T>> Eval<S> toSet(Supplier<S> factory);

    public <K, V> Eval<MapX<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper);

    /**
     * Lazyly convert  this Stream into a List
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
    public Eval<List<T>> toList();

    /**
     * @return Last value in this Stream (must be non-empty)
     */
    public Eval<T> lastValue();

    /**
     * @return the only entry in this Stream if it is a single entry Stream,
     *         otherwise throws an UnsupportedOperationException
     */
    public Eval<T> single();

    /**
     * @return the only entry in this Stream if it is a single entry Stream,
     *         otherwise throws an UnsupportedOperationException
     */
    public Eval<T> single(Predicate<T> predicate);

    /**
     * @return the only entry in this Stream if it is a single entry Stream,
     *         otherwise throws an UnsupportedOperationException
     */
    public Eval<Optional<T>> singleOptional();

    /**
     * Lazyly convert  this Stream into a List
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
    public Eval<Set<T>> toSet();

    /**
     * Lazyly capture the minimum value in this stream using the provided function
     * 
     * @see org.jooq.lambda.Seq#minBy(Function)
     */
    public <U extends Comparable<? super U>> Eval<Optional<T>> minBy(Function<? super T, ? extends U> function);

    /**
     * Lazyly capture the maximum value in this stream using the provided function
     * 
     *  @see org.jooq.lambda.Seq#maxBy(Function)
     */
    public <U extends Comparable<? super U>> Eval<Optional<T>> maxBy(Function<? super T, ? extends U> function);

    /**
     * Lazyly perform a Stream collection
     * 
     * @see java.util.stream.Stream#collect(Collector)
     * 
     */
    public <R, A> Eval<R> collect(Collector<? super T, A, R> collector);

    /**
     *  Lazyly perform a Stream collection
     * @see org.jooq.lambda.Seq#toCollection(Supplier)
     */
    public <C extends Collection<T>> Eval<C> toCollection(Supplier<C> collectionFactory);

    /**
     * Lazyly generate an Array
     * 
     * @see java.util.stream.Stream#toArray(IntFunction)
     */
    public <A> Eval<A[]> toArray(IntFunction<A[]> generator);

    /**
     * Lazyly generate an Array
     * 
     * @see java.util.stream.Stream#toArray(IntFunction)
     */
    public Eval<Object[]> toArray();

    /**
     * Perform an Lazy groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function)
     */
    public <K> Eval<Map<K, List<T>>> groupBy(Function<? super T, ? extends K> classifier);

    /**
     * Perform an Lazy groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function, Collector)
     */
    public <K, A, D> Eval<Map<K, D>> groupBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);

    /**
     * Perform an Lazy groupBy operation
     * @see org.jooq.lambda.Seq#groupBy(Function, Supplier, Collector)
     */
    public <K, D, A, M extends Map<K, D>> Eval<M> groupBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory,
            Collector<? super T, A, D> downstream);

    /**
     * Perform an Lazy foldLeft operation
     * 	 @see org.jooq.lambda.Seq#foldLeft(Object,BiFunction)
     * */
    public <U> Eval<U> foldLeft(U seed, BiFunction<U, ? super T, U> function);

    /**
     * Perform an Lazy foldRight operation
     * 	 @see org.jooq.lambda.Seq#foldRight(Object,BiFunction)
     * */
    public <U> Eval<U> foldRight(U seed, BiFunction<? super T, U, U> function);

    /**
     * Perform an Lazy min operation
     *  @see java.util.stream.Stream#min(Comparator)
     */
    public Eval<Optional<T>> min(Comparator<? super T> comparator);

    /**
     * Perform an Lazy min operation
     *  @see java.util.stream.Stream#max(Comparator)
     */
    public Eval<Optional<T>> max(Comparator<? super T> comparator);

    /**
     * Lazyly perform a Stream collection
     * 
     * @see java.util.stream.Stream#collect(Supplier, BiConsumer, BiConsumer)
     * 
     */
    public <R> Eval<R> collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner);

    /**
     * Lazyly perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)
     * 
     */
    public <U> Eval<U> reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);

    /**
     * Lazyly perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(BinaryOperator)
     * 
     */
    public Eval<Optional<T>> reduce(BinaryOperator<T> accumulator);

    /**
     * Lazyly perform a Stream reduction
     * 
     * @see java.util.stream.Stream#reduce(Object, BinaryOperator)
     * 
     */
    public Eval<T> reduce(T identity, BinaryOperator<T> accumulator);

    /**
     * Lazyly perform a Stream count
     * 
     * @see java.util.stream.Stream#count()
     * 
     */
    public Eval<Long> count();

    public Eval<String> join(CharSequence sep);

    /**
     * Perform an Lazy join operation
     * 	 @see org.jooq.lambda.Seq#join()
     * */
    public Eval<String> join();

    /**
     * Perform an Lazy join operation
     * 	 @see org.jooq.lambda.Seq#join(CharSequence)
     * */
    public Eval<String> join(CharSequence delimiter, CharSequence prefix, CharSequence suffix);

    /**
     * Perform an Lazy findAny operation
     * 	 @see java.util.stream.Stream#findAny()
     * */
    public Eval<Optional<T>> findAny();

    /**
     * Perform an Lazy findAny operation
     * 	 @see java.util.stream.Stream#findFirst()
     * */
    public Eval<Optional<T>> findFirst();

    /**
     * Perform an Lazy findAny operation
     * 	 @see java.util.stream.Stream#findFirst()
     * */
    public Eval<T> firstValue();

}
