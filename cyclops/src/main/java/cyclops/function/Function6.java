package cyclops.function;


import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import cyclops.control.*;

public interface Function6<T1, T2, T3, T4, T5, T6, R> extends Function1<T1, Function1<T2, Function1<T3,Function1<T4,Function1<T5,Function1<T6, R>>>>>> {

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1,  T2,T3,T4,T5,T6,R> constant(R t) {
        return (a,b,c,d,e,f)-> t;
    }
    public static <T1, T2, T3, T4, T5, T6,R> Function6<T1,  T2,T3,T4,T5,T6,R> lazyConstant(Supplier<R> t) {
        return (a,b,c,d,e,f)-> t.get();
    }

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f);

    default Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>> apply(final T1 s) {
        return Curry.curry6(this)
                    .apply(s);
    }

    default Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>> apply(final T1 s, final T2 s2) {
        return apply(s).apply(s2);
    }

    default Function1<T4, Function1<T5, Function1<T6, R>>> apply(final T1 s, final T2 s2, final T3 s3) {
        return apply(s).apply(s2)
                       .apply(s3);
    }

    default Function1<T5, Function1<T6, R>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return apply(s).apply(s2)
                       .apply(s3)
                       .apply(s4);
    }

    default Function1<T6, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5) {
        return apply(s).apply(s2)
                       .apply(s3)
                       .apply(s4)
                       .apply(s5);
    }
    default Function6<T1, T2, T3, T4, T5, T6, Maybe<R>> lazyLift6() {
        return (s1, s2, s3, s4, s5,s6) ->  Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4,s5,s6))));
    }
    default Function6<T1, T2, T3, T4, T5, T6, Future<R>> lift6(Executor ex) {

        return (s1, s2, s3, s4, s5,s6) -> Future.of(() -> apply(s1, s2, s3, s4, s5,s6), ex);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Try<R, Throwable>> liftTry6() {
        return (s1, s2, s3, s4, s5,s6) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5,s6), Throwable.class);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Option<R>> lift6() {

        return (s1, s2, s3, s4, s5, s6) -> Option.ofNullable(apply(s1, s2, s3, s4, s5, s6));
    }

    default Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5,Function1<? super T6, ? extends R>>>>>> curry() {
        return CurryVariance.curry6(this);
    }

    default <V> Function6<T1, T2, T3, T4, T5, T6,V> andThen6(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4,t5,t6)-> after.apply(apply(t1,t2,t3,t4,t5,t6));
    }

    public static <T1, T2, T3,T4,T5,T6,R> Function6<T1,T2,T3,T4,T5,T6,R> λ(final Function6<T1,T2,T3,T4,T5,T6,R> func){
        return func;
    }

    public static <T1, T2, T3,T4,T5,T6,R> Function6<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? extends R> λv(final Function6<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? extends R> triFunc){
        return triFunc;
    }
}
