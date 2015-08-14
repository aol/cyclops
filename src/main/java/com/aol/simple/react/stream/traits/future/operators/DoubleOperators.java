package com.aol.simple.react.stream.traits.future.operators;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

public interface DoubleOperators<T>  extends HasStream<T>, HasExec{
	
	default  CompletableFuture<Double> sumDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.sum(),getExec());
				
	}
	default  CompletableFuture<OptionalDouble> maxDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.max(),getExec());
				
	}
	default  CompletableFuture<OptionalDouble> minDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.min(),getExec());
				
	}
	default CompletableFuture<OptionalDouble> averageDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.average(),getExec());
				
	}
	default  CompletableFuture<DoubleSummaryStatistics> summaryStatisticsDouble(ToDoubleFunction<T> fn){
		
		return CompletableFuture.supplyAsync(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.summaryStatistics(),getExec());
			
	}
}
