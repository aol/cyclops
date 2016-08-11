package com.aol.cyclops.types.stream.lazy;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.types.stream.HasStream;

public interface LongOperatorsMixin<T> extends LongOperators<T>, HasStream<T> {

    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#sum()
     * */
    default Eval<Long> sumLong(ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .sum());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#max()
     * */
    default Eval<OptionalLong> maxLong(ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .max());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#min()
     * */
    default Eval<OptionalLong> minLong(ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .min());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#average()
     * */
    default Eval<OptionalDouble> averageLong(ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .average());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#summaryStatistics()
     * */
    default Eval<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .summaryStatistics());

    }
}
