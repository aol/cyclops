package cyclops.function;


import java.util.concurrent.Executor;
import java.util.function.Function;

import cyclops.control.*;

public interface Function5<T1, T2, T3, T4, T5, R> extends Function1<T1, Function1<T2, Function1<T3,Function1<T4,Function1<T5, R>>>>> {

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e);

    default Function5<T1, T2, T3, T4, T5, Maybe<R>> lazyLift5() {
        return (s1, s2, s3, s4, s5) -> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4,s5))));
    }

    default Function1<T2, Function1<T3, Function1<T4, Function1<T5, R>>>> apply(final T1 s) {
        return Curry.curry5(this)
                    .apply(s);
    }

    default Function1<T3, Function1<T4, Function1<T5, R>>> apply(final T1 s, final T2 s2) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2);
    }

    default Function1<T4, Function1<T5, R>> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }

    default Function1<T5, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4);
    }


    default Function5<T1, T2, T3, T4, T5, Future<R>> lift5(Executor ex) {

        return (s1, s2, s3, s4, s5) -> Future.of(() -> apply(s1, s2, s3, s4, s5), ex);
    }

    default Function5<T1, T2, T3, T4, T5, Try<R, Throwable>> liftTry5() {
        return (s1, s2, s3, s4, s5) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5), Throwable.class);
    }

    default Function5<T1, T2, T3, T4, T5, Option<R>> lift5() {

        return (s1, s2, s3, s4, s5) -> Option.ofNullable(apply(s1, s2, s3, s4, s5));
    }

    default <V> Function5<T1, T2, T3, T4,T5, V> andThen5(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4,t5)-> after.apply(apply(t1,t2,t3,t4,t5));
    }
    default Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5, ? extends R>>>>> curry() {
        return CurryVariance.curry5(this);
    }
    public static <T1, T2, T3,T4,T5,R> Function5<T1,T2,T3,T4,T5,R> λ(final Function5<T1,T2,T3,T4,T5,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,T5,R> Function5<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> λv(final Function5<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> triFunc){
        return triFunc;
    }
}
