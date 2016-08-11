package com.aol.cyclops.types.stream.future;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;

import com.aol.cyclops.types.stream.HasExec;
import com.aol.cyclops.types.stream.HasStream;

public interface LongOperatorsMixin<T> extends LongOperators<T>, HasStream<T>, HasExec {

    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#sum()
     * */
    default CompletableFuture<Long> sumLong(ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .sum(),
                                             getExec());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#max()
     * */
    default CompletableFuture<OptionalLong> maxLong(ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .max(),
                                             getExec());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#min()
     * */
    default CompletableFuture<OptionalLong> minLong(ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .min(),
                                             getExec());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#average()
     * */
    default CompletableFuture<OptionalDouble> averageLong(ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .average(),
                                             getExec());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#summaryStatistics()
     * */
    default CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .summaryStatistics(),
                                             getExec());

    }
}
