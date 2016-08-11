package com.aol.cyclops.types.stream.lazy;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.ToLongFunction;

import com.aol.cyclops.control.Eval;

public interface LongOperators<T> {

    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#sum()
     * */
    Eval<Long> sumLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#max()
     * */
    Eval<OptionalLong> maxLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#min()
     * */
    Eval<OptionalLong> minLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#average()
     * */
    Eval<OptionalDouble> averageLong(ToLongFunction<? super T> fn);

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#summaryStatistics()
     * */
    Eval<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<? super T> fn);
}
