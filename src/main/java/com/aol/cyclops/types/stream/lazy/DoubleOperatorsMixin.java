package com.aol.cyclops.types.stream.lazy;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.types.stream.HasStream;

public interface DoubleOperatorsMixin<T> extends DoubleOperators<T>, HasStream<T> {
    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#sum()
     * */
    @Override
    default Eval<Double> sumDouble(final ToDoubleFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                           .sum());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#max()
     * */
    @Override
    default Eval<OptionalDouble> maxDouble(final ToDoubleFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                           .max());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#min()
     * */
    @Override
    default Eval<OptionalDouble> minDouble(final ToDoubleFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                           .min());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#average()
     * */
    @Override
    default Eval<OptionalDouble> averageDouble(final ToDoubleFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                           .average());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#summaryStatistics()
     * */
    @Override
    default Eval<DoubleSummaryStatistics> summaryStatisticsDouble(final ToDoubleFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToDouble(t -> DoubleStream.of(fn.applyAsDouble(t)))
                                           .summaryStatistics());

    }
}
