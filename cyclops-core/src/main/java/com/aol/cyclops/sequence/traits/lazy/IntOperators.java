package com.aol.cyclops.sequence.traits.lazy;

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.ToIntFunction;

import com.aol.cyclops.control.Eval;

public interface IntOperators<T> {
	
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#sum()
	 * */
	Eval<Integer> sumInt(ToIntFunction<? super T> fn);
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#max()
	 * */
	Eval<OptionalInt> maxInt(ToIntFunction<? super T> fn);
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#min()
	 * */
	Eval<OptionalInt> minInt(ToIntFunction<? super T> fn);
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#average()
	 * */
	Eval<OptionalDouble> averageInt(ToIntFunction<? super T> fn);
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToInt(ToIntFunction)
	 * 	 @see java.util.stream.IntStream#summaryStatistics()
	 * */
	Eval<IntSummaryStatistics> summaryStatisticsInt(ToIntFunction<? super T> fn);
}
