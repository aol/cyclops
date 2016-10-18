package com.aol.cyclops.util.function;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class UncurryConsumer extends Curry {

    public static <T1, T2> BiConsumer<T1, T2> uncurryC2(final Function<T2, Consumer<T1>> biCon) {
        return (t1, t2) -> biCon.apply(t2)
                                .accept(t1);
    }

    public static <T1, T2, T3> TriConsumer<T1, T2, T3> uncurryC3(final Function<T3, Function<T2, Consumer<T1>>> triCon) {
        return (t1, t2, t3) -> triCon.apply(t3)
                                     .apply(t2)
                                     .accept(t1);
    }

    public static <T1, T2, T3, T4> QuadConsumer<T1, T2, T3, T4> uncurryC4(final Function<T4, Function<T3, Function<T2, Consumer<T1>>>> quadCon) {
        return (t1, t2, t3, t4) -> quadCon.apply(t4)
                                          .apply(t3)
                                          .apply(t2)
                                          .accept(t1);
    }

    public static <T1, T2, T3, T4, T5> QuintConsumer<T1, T2, T3, T4, T5> uncurryC5(
            final Function<T5, Function<T4, Function<T3, Function<T2, Consumer<T1>>>>> quintCon) {
        return (t1, t2, t3, t4, t5) -> quintCon.apply(t5)
                                               .apply(t4)
                                               .apply(t3)
                                               .apply(t2)
                                               .accept(t1);
    }

}
