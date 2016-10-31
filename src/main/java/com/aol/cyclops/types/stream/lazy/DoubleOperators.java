package com.aol.cyclops.types.stream.lazy;

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;

import com.aol.cyclops.control.Eval;

public interface DoubleOperators<T> {
    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * @see java.util.stream.DoubleStream#sum()
     * 
     * */
    Eval<Double> sumDouble(ToDoubleFunction<? super T> fn);

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#max()
     * */
    Eval<OptionalDouble> maxDouble(ToDoubleFunction<? super T> fn);

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#min()
     * */
    Eval<OptionalDouble> minDouble(ToDoubleFunction<? super T> fn);

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#average()
     * */
    Eval<OptionalDouble> averageDouble(ToDoubleFunction<? super T> fn);

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToDouble(ToDoubleFunction)
     * 	 @see java.util.stream.DoubleStream#summaryStatistics()
     * */
    Eval<DoubleSummaryStatistics> summaryStatisticsDouble(ToDoubleFunction<? super T> fn);
}
