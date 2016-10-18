package com.aol.cyclops.util.function;

import java.util.function.Function;

public interface OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> {

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f, T7 g, T8 h);

    default Function<T2, Function<T3, Function<T4, Function<T5, Function<T6, Function<T7, Function<T8, R>>>>>>> apply(final T1 s) {
        return Curry.curry8(this)
                    .apply(s);
    }

    default Function<T3, Function<T4, Function<T5, Function<T6, Function<T7, Function<T8, R>>>>>> apply(final T1 s, final T2 s2) {
        return Curry.curry8(this)
                    .apply(s)
                    .apply(s2);
    }

    default Function<T4, Function<T5, Function<T6, Function<T7, Function<T8, R>>>>> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry8(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }

    default Function<T5, Function<T6, Function<T7, Function<T8, R>>>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return Curry.curry8(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4);
    }

    default Function<T6, Function<T7, Function<T8, R>>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5) {
        return Curry.curry8(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5);
    }

    default Function<T7, Function<T8, R>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5, final T6 s6) {
        return Curry.curry8(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5)
                    .apply(s6);
    }

    default Function<T8, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5, final T6 s6, final T7 s7) {
        return Curry.curry8(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5)
                    .apply(s6)
                    .apply(s7);
    }
}
