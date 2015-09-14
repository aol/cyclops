package com.aol.cyclops.sequence.future;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;

public interface LongOperators<T> {
	
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#sum()
	 * */
	CompletableFuture<Long> sumLong(ToLongFunction<T> fn);
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#max()
	 * */
	 CompletableFuture<OptionalLong> maxLong(ToLongFunction<T> fn);
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#min()
	 * */
	CompletableFuture<OptionalLong> minLong(ToLongFunction<T> fn);
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#average()
	 * */
	CompletableFuture<OptionalDouble> averageLong(ToLongFunction<T> fn);
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#summaryStatistics()
	 * */
	CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<T> fn);
}
