package cyclops.function;


import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import cyclops.control.*;

public interface Function5<T1, T2, T3, T4, T5, R> extends Function1<T1, Function1<T2, Function1<T3,Function1<T4,Function1<T5, R>>>>> {

    public static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> constant(R t) {
        return (a,b,c,d,e)-> t;
    }
    public static <T1,T2,T3,T4,T5,R> Function5<T1,  T2,T3,T4,T5,R> lazyConstant(Supplier<R> t) {
        return (a,b,c,d,e)-> t.get();
    }

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e);

    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____1(Function<T1,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____2(Function<T2,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____3(Function<T3,R> fn ) {
        return (a,b,c,d,e)->fn.apply(c);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____4(Function<T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____5(Function<T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____12(Function2<T1,T2,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____13(Function2<T1,T3,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,c);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R>  ____14(Function2<T1,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____15(Function2<T1,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____23(Function2<T2,T3,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,c);
    }

    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____24(Function2<T2,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____25(Function2<T2,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____34(Function2<T3,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(c,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____35(Function2<T3,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(c,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____45(Function2<T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(d,e);
    }

    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____123(Function3<T1,T2,T3,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b,c);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____124(Function3<T1,T2,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____125(Function3<T1,T2,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____134(Function3<T1,T3,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,c,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____135(Function3<T1,T3,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,c,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____145(Function3<T1,T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,d,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____345(Function3<T3,T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(c,d,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____245(Function3<T2,T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,d,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____234(Function3<T2,T3,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,c,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____235(Function3<T2,T3,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,c,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____1234(Function4<T1,T2,T3,T4,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b,c,d);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____1345(Function4<T1,T3,T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,c,d,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____1245(Function4<T1,T2,T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b,d,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____1235(Function4<T1,T2,T3,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(a,b,c,e);
    }
    static <T1,T2,T3,T4,T5, R> Function5<T1,  T2,T3,T4,T5,R> ____2345(Function4<T2,T3,T4,T5,R> fn ) {
        return (a,b,c,d,e)->fn.apply(b,c,d,e);
    }
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
