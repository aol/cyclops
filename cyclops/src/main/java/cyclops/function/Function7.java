package cyclops.function;


import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import cyclops.control.*;

public interface Function7<T1, T2, T3, T4, T5, T6, T7, R> extends Function1<T1, Function1<T2, Function1<T3,Function1<T4,Function1<T5,Function1<T6, Function1<T7,R>>>>>>> {
    /**
     * Create a curried function with arity of 7
     *
     * e.g. with Lombok val
     *
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param func7
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6,T7,R> Function7<T1,T2,T3, T4, T5,T6,T7,R> λ(final Function7<T1,T2,T3,T4,T5,T6,T7, R> func7) {
        return func7;
    }
    public static <T1, T2, T3, T4, T5, T6,T7,R> Function7<? super T1,? super T2,? super T3, ? super T4, ? super T5,? super T6,? super T7,? extends R> v(final Function7<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? super T7, ? extends R> func7) {
        return func7;
    }

    public static <T1, T2, T3, T4, T5, T6,T7, R> Function7<T1,  T2,T3,T4,T5,T6,T7,R> constant(R t) {
        return (a,b,c,d,e,f,g)-> t;
    }
    public static <T1, T2, T3, T4, T5, T6,T7,R> Function7<T1,  T2,T3,T4,T5,T6,T7,R> lazyConstant(Supplier<R> t) {
        return (a,b,c,d,e,f,g)-> t.get();
    }

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f, T7 g);

    default Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, R>>>>>> apply(final T1 s) {
        return Curry.curry7(this)
                    .apply(s);
    }

    default Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, R>>>>> apply(final T1 s, final T2 s2) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2);
    }

    default Function1<T4, Function1<T5, Function1<T6, Function1<T7, R>>>> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }

    default Function1<T5, Function1<T6, Function1<T7, R>>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4);
    }

    default Function1<T6, Function1<T7, R>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5);
    }

    default Function1<T7, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5, final T6 s6) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5)
                    .apply(s6);
    }

    default <V> Function7<T1, T2, T3, T4, T5, T6,T7,V> andThen7(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4,t5,t6,t7)-> after.apply(apply(t1,t2,t3,t4,t5,t6,t7));
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Maybe<R>> lazyLift7() {
        return (s1, s2, s3, s4, s5,s6,s7) -> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4,s5,s6,s7))));
    }
    default Function7<T1, T2, T3, T4, T5, T6, T7,Future<R>> lift7(Executor ex) {

        return (s1, s2, s3, s4, s5,s6,s7) -> Future.of(() -> apply(s1, s2, s3, s4, s5,s6,s7), ex);
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Try<R, Throwable>> liftTry7() {
        return (s1, s2, s3, s4, s5,s6,s7) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5,s6,s7), Throwable.class);
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Option<R>> lift7() {

        return (s1, s2, s3, s4, s5, s6,s7) -> Option.ofNullable(apply(s1, s2, s3, s4, s5, s6,s7));
    }

    default Function1<? super T1, Function1<? super T2, Function1<? super T3, Function1<? super T4, Function1<? super T5,Function1<? super T6,Function1<? super T7, ? extends R>>>>>>> curry() {
        return CurryVariance.curry7(this);
    }

}
