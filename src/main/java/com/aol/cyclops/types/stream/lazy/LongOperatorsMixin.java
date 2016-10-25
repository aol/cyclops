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
    @Override
    default Eval<Long> sumLong(final ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .sum());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#max()
     * */
    @Override
    default Eval<OptionalLong> maxLong(final ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .max());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#min()
     * */
    @Override
    default Eval<OptionalLong> minLong(final ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .min());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#average()
     * */
    @Override
    default Eval<OptionalDouble> averageLong(final ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .average());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToLong(ToLongFunction)
     * 	 @see java.util.stream.LongStream#summaryStatistics()
     * */
    @Override
    default Eval<LongSummaryStatistics> summaryStatisticsLong(final ToLongFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToLong(t -> LongStream.of(fn.applyAsLong(t)))
                                           .summaryStatistics());

    }
}
