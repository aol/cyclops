package cyclops.function;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CurryVariance {

    public static <T1, R> Function1<T1, Supplier<? extends R>> curry(final Function1<? super T1, ? extends R> func) {
        return t1 -> () -> func.apply(t1);
    }

    public static <T1, T2, R> Function1<? super T1, Function1<? super T2, ? extends R>> curry2(
            final BiFunction<? super T1, ? super T2, ? extends R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

    public static <T1, T2, T3, R> Function1<? super T1, Function1<? super T2, Function1<? super T3, ? extends R>>> curry3(
            final Fn3<? super T1, ? super T2, ? super T3, ? extends R> triFunc) {
        return t1 -> t2 -> t3 -> triFunc.apply(t1, t2, t3);
    }

    public static <T1, T2, T3, T4, R> Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, ? extends R>>>> curry4(
            final Fn4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> quadFunc) {
        return t1 -> t2 -> t3 -> t4 -> quadFunc.apply(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5, R> Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5, ? extends R>>>>> curry5(
            final Fn5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> pentFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> pentFunc.apply(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5, Function1<? super T6, ? extends R>>>>>> curry6(
            final Fn6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> hexFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5, Function1<? super T6, Function1<? super T7, ? extends R>>>>>>> curry7(
            final Fn7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> heptFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5, Function1<? super T6, Function1<? super T7, Function1<? super T8, ? extends R>>>>>>>> curry8(
            final Fn8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> octFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

}
