package com.aol.cyclops.types.stream.future;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import com.aol.cyclops.types.stream.HasExec;
import com.aol.cyclops.types.stream.HasStream;

public interface DoubleOperatorsMixin<T> extends DoubleOperators<T>, HasStream<T>, HasExec {
    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#sum()
     * */
    @Override
    default CompletableFuture<Double> sumDouble(final ToDoubleFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                                              .sum(),
                                             getExec());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#max()
     * */
    @Override
    default CompletableFuture<OptionalDouble> maxDouble(final ToDoubleFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                                              .max(),
                                             getExec());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#min()
     * */
    @Override
    default CompletableFuture<OptionalDouble> minDouble(final ToDoubleFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                                              .min(),
                                             getExec());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#average()
     * */
    @Override
    default CompletableFuture<OptionalDouble> averageDouble(final ToDoubleFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                                              .average(),
                                             getExec());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#summaryStatistics()
     * */
    @Override
    default CompletableFuture<DoubleSummaryStatistics> summaryStatisticsDouble(final ToDoubleFunction<? super T> fn) {

        return CompletableFuture.supplyAsync(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                                              .summaryStatistics(),
                                             getExec());

    }
}
