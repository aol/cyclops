package com.aol.cyclops.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

//#http://java.dzone.com/articles/whats-wrong-java-8-currying-vs
public class Curry extends CurryConsumer {

    public static <T1, R> Function<T1, Supplier<R>> curry(final Function<T1, R> func) {
        return t1 -> () -> func.apply(t1);
    }

    public static <T1, T2, R> Function<? super T1, Function<T2, R>> curry2(final BiFunction<? super T1, ? super T2, ? extends R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

    public static <T1, T2, T3, R> Function<T1, Function<T2, Function<T3, R>>> curry3(final TriFunction<T1, T2, T3, R> triFunc) {
        return t1 -> t2 -> t3 -> triFunc.apply(t1, t2, t3);
    }

    public static <T1, T2, T3, T4, R> Function<T1, Function<T2, Function<T3, Function<T4, R>>>> curry4(
            final QuadFunction<T1, T2, T3, T4, R> quadFunc) {
        return t1 -> t2 -> t3 -> t4 -> quadFunc.apply(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5, R> Function<T1, Function<T2, Function<T3, Function<T4, Function<T5, R>>>>> curry5(
            final QuintFunction<T1, T2, T3, T4, T5, R> pentFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> pentFunc.apply(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function<T1, Function<T2, Function<T3, Function<T4, Function<T5, Function<T6, R>>>>>> curry6(
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function<T1, Function<T2, Function<T3, Function<T4, Function<T5, Function<T6, Function<T7, R>>>>>>> curry7(
            final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function<T1, Function<T2, Function<T3, Function<T4, Function<T5, Function<T6, Function<T7, Function<T8, R>>>>>>>> curry8(
            final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

}
