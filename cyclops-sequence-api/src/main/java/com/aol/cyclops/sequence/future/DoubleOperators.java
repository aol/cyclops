package com.aol.cyclops.sequence.future;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

public interface DoubleOperators<T>{
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#sum()
	 * */
	CompletableFuture<Double> sumDouble(ToDoubleFunction<? super T> fn);
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#max()
	 * */
	CompletableFuture<OptionalDouble> maxDouble(ToDoubleFunction<? super T> fn);
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#min()
	 * */
	CompletableFuture<OptionalDouble> minDouble(ToDoubleFunction<? super T> fn);
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#average()
	 * */
	CompletableFuture<OptionalDouble> averageDouble(ToDoubleFunction<? super T> fn);
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#summaryStatistics()
	 * */
	 CompletableFuture<DoubleSummaryStatistics> summaryStatisticsDouble(ToDoubleFunction<? super T> fn);
}
