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
    default CompletableFuture<Integer> sumInt(ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .sum(),
                                             getExec());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#max()
     * */
    default CompletableFuture<OptionalInt> maxInt(ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .max(),
                                             getExec());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#min()
     * */
    default CompletableFuture<OptionalInt> minInt(ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .min(),
                                             getExec());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#average()
     * */
    default CompletableFuture<OptionalDouble> averageInt(ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .average(),
                                             getExec());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#summaryStatistics()
     * */
    default CompletableFuture<IntSummaryStatistics> summaryStatisticsInt(ToIntFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                                              .summaryStatistics(),
                                             getExec());

    }
}
