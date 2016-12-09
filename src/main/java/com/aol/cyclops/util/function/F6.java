package com.aol.cyclops.util.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;

public interface F6<T1, T2, T3, T4, T5, T6, R> {

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f);

    default Function<T2, Function<T3, Function<T4, Function<T5, Function<T6, R>>>>> apply(final T1 s) {
        return Curry.curry6(this)
                    .apply(s);
    }

    default Function<T3, Function<T4, Function<T5, Function<T6, R>>>> apply(final T1 s, final T2 s2) {
        return apply(s).apply(s2);
    }

    default Function<T4, Function<T5, Function<T6, R>>> apply(final T1 s, final T2 s2, final T3 s3) {
        return apply(s).apply(s2)
                       .apply(s3);
    }

    default Function<T5, Function<T6, R>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return apply(s).apply(s2)
                       .apply(s3)
                       .apply(s4);
    }

    default Function<T6, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5) {
        return apply(s).apply(s2)
                       .apply(s3)
                       .apply(s4)
                       .apply(s5);
    }
    default F6<T1, T2, T3, T4, T5, T6, Maybe<R>> lift() {
        return (s1, s2, s3, s4, s5,s6) -> Maybe.fromEval(Eval.later(() -> apply(s1, s2, s3, s4, s5,s6)));
    }
    default F6<T1, T2, T3, T4, T5, T6, FutureW<R>> lift(Executor ex) {

        return (s1, s2, s3, s4, s5,s6) -> FutureW.ofSupplier(() -> apply(s1, s2, s3, s4, s5,s6), ex);
    }

    default F6<T1, T2, T3, T4, T5, T6, Try<R, Throwable>> liftTry() {
        return (s1, s2, s3, s4, s5,s6) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5,s6), Throwable.class);
    }

    default F6<T1, T2, T3, T4, T5, T6, Optional<R>> liftOpt() {

        return (s1, s2, s3, s4, s5, s6) -> Optional.ofNullable(apply(s1, s2, s3, s4, s5, s6));
    }

    default Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5,Function<? super T6, ? extends R>>>>>> curry() {
        return CurryVariance.curry6(this);
    }
    public static <T1, T2, T3,T4,T5,T6,R> F6<T1,T2,T3,T4,T5,T6,R> λ(final F6<T1,T2,T3,T4,T5,T6,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,T5,T6,R> F6<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? extends R> λv(final F6<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? extends R> triFunc){
        return triFunc;
    }
}
