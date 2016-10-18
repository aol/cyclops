package com.aol.cyclops.types.stream.future;

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import com.aol.cyclops.types.stream.HasExec;
import com.aol.cyclops.types.stream.HasStream;

public interface IntOperatorsMixin<T> extends IntOperators<T>, HasStream<T>, HasExec {

    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#sum()
     * */
    @Override
    default CompletableFuture<Integer> sumInt(final ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .sum(),
                                             getExec());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#max()
     * */
    @Override
    default CompletableFuture<OptionalInt> maxInt(final ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .max(),
                                             getExec());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#min()
     * */
    @Override
    default CompletableFuture<OptionalInt> minInt(final ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .min(),
                                             getExec());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#average()
     * */
    @Override
    default CompletableFuture<OptionalDouble> averageInt(final ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .average(),
                                             getExec());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#summaryStatistics()
     * */
    @Override
    default CompletableFuture<IntSummaryStatistics> summaryStatisticsInt(final ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .summaryStatistics(),
                                             getExec());

    }
}
