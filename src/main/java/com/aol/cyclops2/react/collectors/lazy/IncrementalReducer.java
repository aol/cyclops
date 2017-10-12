package com.aol.cyclops2.react.collectors.lazy;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


import com.aol.cyclops2.internal.react.async.future.FastFuture;
import com.aol.cyclops2.internal.react.stream.MissingValue;
import com.aol.cyclops2.types.futurestream.BlockingStream;

import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Perform incremental (optionally parallel) reduction on a reactiveStream
 * 
 * @author johnmcclean
 *
 * @param <T> Data type
 */
@Getter
@AllArgsConstructor
public class IncrementalReducer<T> {
    private final LazyResultConsumer<T> consumer;
    private final BlockingStream<T> blocking;

    public void forEach(final Consumer<? super T> c, final Function<FastFuture, T> safeJoin) {

        forEachResults(consumer.getResults(), c, safeJoin);

    }

    public void forEachResults(final Collection<FastFuture<T>> results, final Consumer<? super T> c, final Function<FastFuture, T> safeJoin) {
        final Stream<FastFuture<T>> streamToUse = results.stream();
        streamToUse.map(safeJoin)
                   .filter(v -> v != MissingValue.MISSING_VALUE)
                   .forEach(c);
        consumer.getResults()
                .clear();
    }

    public T reduce(final Function<FastFuture, T> safeJoin, final T identity, final BinaryOperator<T> accumulator) {
        return reduceResults(consumer.getResults(), safeJoin, identity, accumulator);

    }

    public T reduceResults(final Collection<FastFuture<T>> results, final Function<FastFuture, T> safeJoin, final T identity,
            final BinaryOperator<T> accumulator) {
        final Stream<FastFuture<T>> streamToUse = results.stream();

        final T result = streamToUse.map(safeJoin)
                                    .filter(v -> v != MissingValue.MISSING_VALUE)
                                    .reduce(identity, accumulator);
        consumer.getResults()
                .clear();
        return result;
    }

    public Optional<T> reduce(final Function<FastFuture, T> safeJoin, final BinaryOperator<T> accumulator) {

        return reduceResults(consumer.getResults(), safeJoin, accumulator);

    }

    public Optional<T> reduceResults(final Collection<FastFuture<T>> results, final Function<FastFuture, T> safeJoin,
            final BinaryOperator<T> accumulator) {
        final Stream<FastFuture<T>> streamToUse = results.stream();

        final Optional<T> result = streamToUse.map(safeJoin)
                                              .filter(v -> v != MissingValue.MISSING_VALUE)
                                              .reduce(accumulator);
        consumer.getResults()
                .clear();

        return result;
    }

    public <U> U reduce(final Function<FastFuture, T> safeJoin, final U identity, final BiFunction<U, ? super T, U> accumulator) {

        return reduceResults(consumer.getResults(), safeJoin, identity, accumulator);

    }

    public <U> U reduce(final Function<FastFuture, T> safeJoin, final U identity, final BiFunction<U, ? super T, U> accumulator,
            final BinaryOperator<U> combiner) {

        return reduceResults(consumer.getResults(), safeJoin, identity, accumulator, combiner);

    }

    public <U> U reduceResults(final Collection<FastFuture<T>> results, final Function<FastFuture, T> safeJoin, final U identity,
            final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        final Stream<FastFuture<T>> streamToUse = results.stream();

        final U result = streamToUse.map(safeJoin)
                                    .filter(v -> v != MissingValue.MISSING_VALUE)
                                    .reduce(identity, accumulator, combiner);
        consumer.getResults()
                .clear();
        return result;
    }

    public <U> U reduceResults(final Collection<FastFuture<T>> results, final Function<FastFuture, T> safeJoin, final U identity,
            final BiFunction<U, ? super T, U> accumulator) {
        final Stream<FastFuture<T>> streamToUse = results.stream();

        final U result = ReactiveSeq.fromStream(streamToUse)
                            .map(safeJoin)
                            .filter(v -> v != MissingValue.MISSING_VALUE)
                            .foldLeft(identity, accumulator);
        consumer.getResults()
                .clear();
        return result;
    }
}
