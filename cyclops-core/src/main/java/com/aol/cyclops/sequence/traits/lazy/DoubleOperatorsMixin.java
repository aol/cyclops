package com.aol.cyclops.sequence.traits.lazy;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.streams.HasExec;
import com.aol.cyclops.streams.HasStream;

public interface DoubleOperatorsMixin<T>  extends  DoubleOperators<T>,HasStream<T>{
	/**
	 * Perform an asynchronous sum operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#sum()
	 * */
	default  Eval<Double> sumDouble(ToDoubleFunction<? super T> fn){
		
		return Eval.later(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.sum());
				
	}
	/**
	 * Perform an asynchronous max operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#max()
	 * */
	default  Eval<OptionalDouble> maxDouble(ToDoubleFunction<? super T> fn){
		
		return Eval.later(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.max());
				
	}
	/**
	 * Perform an asynchronous min operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#min()
	 * */
	default  Eval<OptionalDouble> minDouble(ToDoubleFunction<? super T> fn){
		
		return Eval.later(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.min());
				
	}
	/**
	 * Perform an asynchronous average operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#average()
	 * */
	default Eval<OptionalDouble> averageDouble(ToDoubleFunction<? super T> fn){
		
		return Eval.later(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.average());
				
	}
	/**
	 * Perform an asynchronous summaryStatistics operation
	 * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
	 * 	 @see java.util.stream.DoubleStream#summaryStatistics()
	 * */
	default  Eval<DoubleSummaryStatistics> summaryStatisticsDouble(ToDoubleFunction<? super T> fn){
		
		return Eval.later(()->getStream()
								.flatMapToDouble(t-> DoubleStream.of(fn.applyAsDouble(t)))
								.summaryStatistics());
			
	}
}
