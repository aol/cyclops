package com.aol.simple.react.stream.traits.future.operators;

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public interface IntOperators<T>  extends HasStream<T>, HasExec{
	
	default  CompletableFuture<Integer> sumInt(ToIntFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToInt(t-> IntStream.of(fn.applyAsInt(t)))
								.sum(),getExec());
				
	}
	default  CompletableFuture<OptionalInt> maxInt(ToIntFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToInt(t-> IntStream.of(fn.applyAsInt(t)))
								.max(),getExec());
				
	}
	default  CompletableFuture<OptionalInt> minInt(ToIntFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToInt(t-> IntStream.of(fn.applyAsInt(t)))
								.min(),getExec());
				
	}
	default CompletableFuture<OptionalDouble> averageInt(ToIntFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToInt(t-> IntStream.of(fn.applyAsInt(t)))
								.average(),getExec());
				
	}
	default  CompletableFuture<IntSummaryStatistics> summaryStatisticsInt(ToIntFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToInt(t-> IntStream.of(fn.applyAsInt(t)))
								.summaryStatistics(),getExec());
			
	}
}
