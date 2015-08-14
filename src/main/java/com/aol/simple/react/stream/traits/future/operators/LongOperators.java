package com.aol.simple.react.stream.traits.future.operators;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;

public interface LongOperators<T>  extends HasStream<T>, HasExec{
	
	default  CompletableFuture<Long> sumLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.sum(),getExec());
				
	}
	default  CompletableFuture<OptionalLong> maxLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.max(),getExec());
				
	}
	default  CompletableFuture<OptionalLong> minLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.min(),getExec());
				
	}
	default CompletableFuture<OptionalDouble> averageLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.average(),getExec());
				
	}
	default  CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToLong(t-> LongStream.of(fn.applyAsLong(t)))
								.summaryStatistics(),getExec());
			
	}
}
