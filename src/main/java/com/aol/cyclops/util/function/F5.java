package com.aol.cyclops.util.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.jooq.lambda.function.Function5;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;

public interface F5<T1, T2, T3, T4, T5, R> extends Function5<T1, T2, T3, T4, T5, R> {

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e);

    default F5<T1, T2, T3, T4, T5, Maybe<R>> lift() {
        return (s1, s2, s3, s4, s5) -> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4,s5))));
    }

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
    

    default F5<T1, T2, T3, T4, T5, FutureW<R>> lift(Executor ex) {

        return (s1, s2, s3, s4, s5) -> FutureW.ofSupplier(() -> apply(s1, s2, s3, s4, s5), ex);
    }

    default F5<T1, T2, T3, T4, T5, Try<R, Throwable>> liftTry() {
        return (s1, s2, s3, s4, s5) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5), Throwable.class);
    }

    default F5<T1, T2, T3, T4, T5, Optional<R>> liftOpt() {

        return (s1, s2, s3, s4, s5) -> Optional.ofNullable(apply(s1, s2, s3, s4, s5));
    }

    default Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, ? extends R>>>>> curry() {
        return CurryVariance.curry5(this);
    }
    public static <T1, T2, T3,T4,T5,R> F5<T1,T2,T3,T4,T5,R> λ(final F5<T1,T2,T3,T4,T5,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,T5,R> F5<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> λv(final F5<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> triFunc){
        return triFunc;
    }
}
