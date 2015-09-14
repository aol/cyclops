package com.aol.cyclops.sequence.future;

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public interface IntOperators<T> {
	
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#sum()
	 * */
	CompletableFuture<Integer> sumInt(ToIntFunction<T> fn);
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#max()
	 * */
	CompletableFuture<OptionalInt> maxInt(ToIntFunction<T> fn);
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#min()
	 * */
	CompletableFuture<OptionalInt> minInt(ToIntFunction<T> fn);
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#average()
	 * */
	CompletableFuture<OptionalDouble> averageInt(ToIntFunction<T> fn);
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#summaryStatistics()
	 * */
	CompletableFuture<IntSummaryStatistics> summaryStatisticsInt(ToIntFunction<T> fn);
}
