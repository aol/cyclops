package com.aol.cyclops.util.function;

import java.util.function.Function;

public interface QuintFunction<T1, T2, T3, T4, T5, R> {

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e);

    default Function<T2, Function<T3, Function<T4, Function<T5, R>>>> apply(final T1 s) {
        return Curry.curry5(this)
                    .apply(s);
    }

    default Function<T3, Function<T4, Function<T5, R>>> apply(final T1 s, final T2 s2) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2);
    }

    default Function<T4, Function<T5, R>> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }

    default Function<T5, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4);
    }
}
