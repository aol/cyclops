package com.aol.simple.react.stream.traits.future.operators;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.DoubleStream;

public interface DoubleOperators<T>  extends HasStream<T>, HasExec{
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#sum()
	 * */
	default  CompletableFuture<Double> sumDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.sum(),getExec());
				
	}
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#max()
	 * */
	default  CompletableFuture<OptionalDouble> maxDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.max(),getExec());
				
	}
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#min()
	 * */
	default  CompletableFuture<OptionalDouble> minDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.min(),getExec());
				
	}
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#average()
	 * */
	default CompletableFuture<OptionalDouble> averageDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.average(),getExec());
				
	}
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#summaryStatistics()
	 * */
	default  CompletableFuture<DoubleSummaryStatistics> summaryStatisticsDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.summaryStatistics(),getExec());
			
	}
}
