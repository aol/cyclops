package com.aol.cyclops.types.stream.future;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToLongFunction;

public interface LongOperators<T> {

    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#sum()
     * */
    CompletableFuture<Long> sumLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#max()
     * */
    CompletableFuture<OptionalLong> maxLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#min()
     * */
    CompletableFuture<OptionalLong> minLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#average()
     * */
    CompletableFuture<OptionalDouble> averageLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#summaryStatistics()
     * */
    CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<? super T> fn);
}
