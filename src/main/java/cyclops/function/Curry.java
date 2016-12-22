package cyclops.function;

import java.util.function.BiFunction;
import java.util.function.Function;

//#http://java.dzone.com/articles/whats-wrong-java-8-currying-vs
public class Curry extends CurryConsumer {

    public static <T1, R> F1<T1, F0<R>> curry(final F1<T1, R> func) {
        return t1 -> () -> func.apply(t1);
    }

    public static <T1, T2, R> F1<T1, F1<T2, R>> curry2(final F2<T1, T2,R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

    public static <T1, T2, T3, R> F1<T1, F1<T2, F1<T3, R>>> curry3(final F3<T1, T2, T3, R> triFunc) {
        return t1 -> t2 -> t3 -> triFunc.apply(t1, t2, t3);
    }

    public static <T1, T2, T3, T4, R> F1<T1, F1<T2, F1<T3, F1<T4, R>>>> curry4(
            final F4<T1, T2, T3, T4, R> quadFunc) {
        return t1 -> t2 -> t3 -> t4 -> quadFunc.apply(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, R>>>>> curry5(
            final F5<T1, T2, T3, T4, T5, R> pentFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> pentFunc.apply(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, R>>>>>> curry6(
            final F6<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, R>>>>>>> curry7(
            final F7<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, F1<T8, R>>>>>>>> curry8(
            final F8<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

}
