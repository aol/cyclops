package com.aol.cyclops.types.stream.future;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

import com.aol.cyclops.data.collections.extensions.standard.MapX;

public interface FutureCollectable<T> {

    /**
    * Collect this collectable into 2 {@link Collector}s.
    */
    default <R1, R2, A1, A2> CompletableFuture<Tuple2<R1, R2>> collect(final Collector<? super T, A1, R1> collector1,
            final Collector<? super T, A2, R2> collector2) {
        return collect(Tuple.collectors(collector1, collector2));
    }

    /**
     * Collect this collectable into 3 {@link Collector}s.
     */
    default <R1, R2, R3, A1, A2, A3> CompletableFuture<Tuple3<R1, R2, R3>> collect(final Collector<? super T, A1, R1> collector1,
            final Collector<? super T, A2, R2> collector2, final Collector<? super T, A3, R3> collector3) {
        return collect(Tuple.collectors(collector1, collector2, collector3));
    }

    /**
     * Collect this collectable into 4 {@link Collector}s.
     */
    default <R1, R2, R3, R4, A1, A2, A3, A4> CompletableFuture<Tuple4<R1, R2, R3, R4>> collect(final Collector<? super T, A1, R1> collector1,
            final Collector<? super T, A2, R2> collector2, final Collector<? super T, A3, R3> collector3,
            final Collector<? super T, A4, R4> collector4) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4));
    }

    /**
     * Collect this collectable into 5 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, A1, A2, A3, A4, A5> CompletableFuture<Tuple5<R1, R2, R3, R4, R5>> collect(
            final Collector<? super T, A1, R1> collector1, final Collector<? super T, A2, R2> collector2,
            final Collector<? super T, A3, R3> collector3, final Collector<? super T, A4, R4> collector4,
            final Collector<? super T, A5, R5> collector5) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5));
    }

    /**
     * Collect this collectable into 6 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, A1, A2, A3, A4, A5, A6> CompletableFuture<Tuple6<R1, R2, R3, R4, R5, R6>> collect(
            final Collector<? super T, A1, R1> collector1, final Collector<? super T, A2, R2> collector2,
            final Collector<? super T, A3, R3> collector3, final Collector<? super T, A4, R4> collector4,
            final Collector<? super T, A5, R5> collector5, final Collector<? super T, A6, R6> collector6) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6));
    }

    /**
     * Collect this collectable into 7 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, R7, A1, A2, A3, A4, A5, A6, A7> CompletableFuture<Tuple7<R1, R2, R3, R4, R5, R6, R7>> collect(
            final Collector<? super T, A1, R1> collector1, final Collector<? super T, A2, R2> collector2,
            final Collector<? super T, A3, R3> collector3, final Collector<? super T, A4, R4> collector4,
            final Collector<? super T, A5, R5> collector5, final Collector<? super T, A6, R6> collector6,
            final Collector<? super T, A7, R7> collector7) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6, collector7));
    }

    /**
     * Collect this collectable into 8 {@link Collector}s.
     */
    default <R1, R2, R3, R4, R5, R6, R7, R8, A1, A2, A3, A4, A5, A6, A7, A8> CompletableFuture<Tuple8<R1, R2, R3, R4, R5, R6, R7, R8>> collect(
            final Collector<? super T, A1, R1> collector1, final Collector<? super T, A2, R2> collector2,
            final Collector<? super T, A3, R3> collector3, final Collector<? super T, A4, R4> collector4,
            final Collector<? super T, A5, R5> collector5, final Collector<? super T, A6, R6> collector6,
            final Collector<? super T, A7, R7> collector7, final Collector<? super T, A8, R8> collector8) {
        return collect(Tuple.collectors(collector1, collector2, collector3, collector4, collector5, collector6, collector7, collector8));
    }

    public <R, A> CompletableFuture<R> collect(Collector<? super T, A, R> collector);

    public CompletableFuture<Long> count();

    public CompletableFuture<Long> countDistinct();

    public <U> CompletableFuture<Long> countDistinctBy(Function<? super T, ? extends U> function);

    public CompletableFuture<Optional<T>> mode();

    public CompletableFuture<Optional<T>> sum();

    public <U> CompletableFuture<Optional<U>> sum(Function<? super T, ? extends U> function);

    public CompletableFuture<Integer> sumInt(ToIntFunction<? super T> function);

    public CompletableFuture<Long> sumLong(ToLongFunction<? super T> function);

    public CompletableFuture<Double> sumDouble(ToDoubleFunction<? super T> function);

    public CompletableFuture<Optional<T>> avg();

    public <U> CompletableFuture<Optional<U>> avg(Function<? super T, ? extends U> function);

    public CompletableFuture<Optional<T>> min();

    public CompletableFuture<Optional<T>> min(Comparator<? super T> comparator);

    public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> min(Function<? super T, ? extends U> function);

    public <U> CompletableFuture<Optional<U>> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> minBy(Function<? super T, ? extends U> function);

    public <U> CompletableFuture<Optional<T>> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public CompletableFuture<Optional<T>> max();

    public CompletableFuture<Optional<T>> max(Comparator<? super T> comparator);

    public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> max(Function<? super T, ? extends U> function);

    public <U> CompletableFuture<Optional<U>> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function);

    public <U> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public CompletableFuture<Optional<T>> median();

    public CompletableFuture<Optional<T>> median(Comparator<? super T> comparator);

    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> medianBy(Function<? super T, ? extends U> function);

    public <U> CompletableFuture<Optional<T>> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator);

    public CompletableFuture<Optional<T>> percentile(double percentile);

    public CompletableFuture<Optional<T>> percentile(double percentile, Comparator<? super T> comparator);

    public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> percentileBy(double percentile,
            Function<? super T, ? extends U> function);

    public <U> CompletableFuture<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function,
            Comparator<? super U> comparator);

    public CompletableFuture<Boolean> allMatch(Predicate<? super T> predicate);

    public CompletableFuture<Boolean> anyMatch(Predicate<? super T> predicate);

    public CompletableFuture<Boolean> noneMatch(Predicate<? super T> predicate);

    public CompletableFuture<List<T>> toList();

    public <L extends List<T>> CompletableFuture<L> toList(Supplier<L> factory);

    public CompletableFuture<Set<T>> toSet();

    public <S extends Set<T>> CompletableFuture<S> toSet(Supplier<S> factory);

    public <C extends Collection<T>> CompletableFuture<C> toCollection(Supplier<C> factory);

    public <K, V> CompletableFuture<MapX<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper);

}
