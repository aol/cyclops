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
    @Override
    default CompletableFuture<Long> sumLong(final ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .sum(),
                                             getExec());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#max()
     * */
    @Override
    default CompletableFuture<OptionalLong> maxLong(final ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .max(),
                                             getExec());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#min()
     * */
    @Override
    default CompletableFuture<OptionalLong> minLong(final ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .min(),
                                             getExec());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#average()
     * */
    @Override
    default CompletableFuture<OptionalDouble> averageLong(final ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .average(),
                                             getExec());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#summaryStatistics()
     * */
    @Override
    default CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(final ToLongFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                                              .summaryStatistics(),
                                             getExec());

    }
}
