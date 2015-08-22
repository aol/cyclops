package com.aol.simple.react.stream.traits.future.operators;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;

public interface LongOperators<T>  extends HasStream<T>, HasExec{
	
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#sum()
	 * */
	default  CompletableFuture<Long> sumLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.sum(),getExec());
				
	}
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#max()
	 * */
	default  CompletableFuture<OptionalLong> maxLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.max(),getExec());
				
	}
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#min()
	 * */
	default  CompletableFuture<OptionalLong> minLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.min(),getExec());
				
	}
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#average()
	 * */
	default CompletableFuture<OptionalDouble> averageLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.average(),getExec());
				
	}
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToLong(ToLongFunction)
	 * 	 @see java.util.stream.LongStream#summaryStatistics()
	 * */
	default  CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.summaryStatistics(),getExec());
			
	}
}
