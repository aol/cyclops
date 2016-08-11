package com.aol.cyclops.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CurryVariance {

    public static <T1, R> Function<T1, Supplier<? extends R>> curry(Function<? super T1, ? extends R> func) {
        return t1 -> () -> func.apply(t1);
    }

    public static <T1, T2, R> Function<? super T1, Function<? super T2, ? extends R>> curry2(BiFunction<? super T1, ? super T2, ? extends R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

    public static <T1, T2, T3, R> Function<? super T1, Function<? super T2, Function<? super T3, ? extends R>>> curry3(
            TriFunction<? super T1, ? super T2, ? super T3, ? extends R> triFunc) {
        return t1 -> t2 -> t3 -> triFunc.apply(t1, t2, t3);
    }

    public static <T1, T2, T3, T4, R> Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, ? extends R>>>> curry4(
            QuadFunction<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> quadFunc) {
        return t1 -> t2 -> t3 -> t4 -> quadFunc.apply(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5, R> Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, ? extends R>>>>> curry5(
            QuintFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> pentFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> pentFunc.apply(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, Function<? super T6, ? extends R>>>>>> curry6(
            HexFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> hexFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, Function<? super T6, Function<? super T7, ? extends R>>>>>>> curry7(
            HeptFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> heptFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, Function<? super T6, Function<? super T7, Function<? super T8, ? extends R>>>>>>>> curry8(
            OctFunction<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> octFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

}
