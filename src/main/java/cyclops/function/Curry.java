package cyclops.function;

import java.util.function.BiFunction;

//#http://java.dzone.com/articles/whats-wrong-java-8-currying-vs
public class Curry extends CurryConsumer {

    public static <T1, R> Fn1<T1, Fn0<R>> curry(final Fn1<T1, R> func) {
        return t1 -> () -> func.apply(t1);
    }

    public static <T1, T2, R> Fn1<T1, Fn1<T2, R>> curry2(final BiFunction<T1, T2,R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

    public static <T1, T2, T3, R> Fn1<T1, Fn1<T2, Fn1<T3, R>>> curry3(final Fn3<T1, T2, T3, R> triFunc) {
        return t1 -> t2 -> t3 -> triFunc.apply(t1, t2, t3);
    }

    public static <T1, T2, T3, T4, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, R>>>> curry4(
            final Fn4<T1, T2, T3, T4, R> quadFunc) {
        return t1 -> t2 -> t3 -> t4 -> quadFunc.apply(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, R>>>>> curry5(
            final Fn5<T1, T2, T3, T4, T5, R> pentFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> pentFunc.apply(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, R>>>>>> curry6(
            final Fn6<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, Fn1<T7, R>>>>>>> curry7(
            final Fn7<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, Fn1<T7, Fn1<T8, R>>>>>>>> curry8(
            final Fn8<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

}
