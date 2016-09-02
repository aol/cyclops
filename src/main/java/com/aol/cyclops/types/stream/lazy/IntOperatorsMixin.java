package com.aol.cyclops.types.stream.lazy;

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.types.stream.HasStream;

public interface IntOperatorsMixin<T> extends IntOperators<T>, HasStream<T> {

    /**
     * Perform an asynchronous sum operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#sum()
     * */
    default Eval<Integer> sumInt(ToIntFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                           .sum());

    }

    /**
     * Perform an asynchronous max operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#max()
     * */
    default Eval<OptionalInt> maxInt(ToIntFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                           .max());

    }

    /**
     * Perform an asynchronous min operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#min()
     * */
    default Eval<OptionalInt> minInt(ToIntFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                           .min());

    }

    /**
     * Perform an asynchronous average operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#average()
     * */
    default Eval<OptionalDouble> averageInt(ToIntFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                           .average());

    }

    /**
     * Perform an asynchronous summaryStatistics operation
     * @see java.util.stream.Stream#mapToInt(ToIntFunction)
     * 	 @see java.util.stream.IntStream#summaryStatistics()
     * */
    default Eval<IntSummaryStatistics> summaryStatisticsInt(ToIntFunction<? super T> fn) {

        return Eval.later(() -> getStream().flatMapToInt(t -> IntStream.of(fn.applyAsInt(t)))
                                           .summaryStatistics());

    }
}
