package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.control.Maybe;
import cyclops.control.Try;

public interface Fn5<T1, T2, T3, T4, T5, R> extends Fn1<T1, Fn1<T2, Fn1<T3,Fn1<T4,Fn1<T5, R>>>>> {

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e);

    default Fn5<T1, T2, T3, T4, T5, Maybe<R>> lift5() {
        return (s1, s2, s3, s4, s5) -> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4,s5))));
    }

    default Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, R>>>> apply(final T1 s) {
        return Curry.curry5(this)
                    .apply(s);
    }

    default Fn1<T3, Fn1<T4, Fn1<T5, R>>> apply(final T1 s, final T2 s2) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2);
    }

    default Fn1<T4, Fn1<T5, R>> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }

    default Fn1<T5, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return Curry.curry5(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4);
    }
    

    default Fn5<T1, T2, T3, T4, T5, Future<R>> lift5(Executor ex) {

        return (s1, s2, s3, s4, s5) -> Future.of(() -> apply(s1, s2, s3, s4, s5), ex);
    }

    default Fn5<T1, T2, T3, T4, T5, Try<R, Throwable>> liftTry5() {
        return (s1, s2, s3, s4, s5) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5), Throwable.class);
    }

    default Fn5<T1, T2, T3, T4, T5, Optional<R>> liftOpt5() {

        return (s1, s2, s3, s4, s5) -> Optional.ofNullable(apply(s1, s2, s3, s4, s5));
    }

    default <V> Fn5<T1, T2, T3, T4,T5, V> andThen5(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4,t5)-> after.apply(apply(t1,t2,t3,t4,t5));
    }
    default Fn1<? super T1, Fn1<? super T2, Fn1<? super T3, Fn1<? super T4, Fn1<? super T5, ? extends R>>>>> curry() {
        return CurryVariance.curry5(this);
    }
    public static <T1, T2, T3,T4,T5,R> Fn5<T1,T2,T3,T4,T5,R> λ(final Fn5<T1,T2,T3,T4,T5,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,T5,R> Fn5<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> λv(final Fn5<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> triFunc){
        return triFunc;
    }
}
