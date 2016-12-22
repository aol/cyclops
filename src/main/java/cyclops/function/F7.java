package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;

public interface F7<T1, T2, T3, T4, T5, T6, T7, R> {
    /**
     * Create a curried function with arity of 7
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param quadFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6,T7,R> F7<T1,T2,T3, T4, T5,T6,T7,R> λ(final F7<T1,T2,T3,T4,T5,T6,T7, R> func7) {
        return func7;
    }
    public static <T1, T2, T3, T4, T5, T6,T7,R> F7<? super T1,? super T2,? super T3, ? super T4, ? super T5,? super T6,? super T7,? extends R> v(final F7<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? super T7, ? extends R> func7) {
        return func7;
    }
    

    public R apply(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f, T7 g);

    default F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, R>>>>>> apply(final T1 s) {
        return Curry.curry7(this)
                    .apply(s);
    }

    default F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, R>>>>> apply(final T1 s, final T2 s2) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2);
    }

    default F1<T4, F1<T5, F1<T6, F1<T7, R>>>> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }

    default F1<T5, F1<T6, F1<T7, R>>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4);
    }

    default F1<T6, F1<T7, R>> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5);
    }

    default F1<T7, R> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4, final T5 s5, final T6 s6) {
        return Curry.curry7(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3)
                    .apply(s4)
                    .apply(s5)
                    .apply(s6);
    }

    default <V> F7<T1, T2, T3, T4, T5, T6,T7,V> andThen(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4,t5,t6,t7)-> after.apply(apply(t1,t2,t3,t4,t5,t6,t7));
    }

    default F7<T1, T2, T3, T4, T5, T6, T7, Maybe<R>> lift() {
        return (s1, s2, s3, s4, s5,s6,s7) -> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4,s5,s6,s7))));
    }
    default F7<T1, T2, T3, T4, T5, T6, T7,FutureW<R>> lift(Executor ex) {

        return (s1, s2, s3, s4, s5,s6,s7) -> FutureW.ofSupplier(() -> apply(s1, s2, s3, s4, s5,s6,s7), ex);
    }

    default F7<T1, T2, T3, T4, T5, T6, T7, Try<R, Throwable>> liftTry() {
        return (s1, s2, s3, s4, s5,s6,s7) -> Try.withCatch(() -> apply(s1, s2, s3, s4, s5,s6,s7), Throwable.class);
    }

    default F7<T1, T2, T3, T4, T5, T6, T7, Optional<R>> liftOpt() {

        return (s1, s2, s3, s4, s5, s6,s7) -> Optional.ofNullable(apply(s1, s2, s3, s4, s5, s6,s7));
    }

    default F1<? super T1, F1<? super T2, F1<? super T3, F1<? super T4, F1<? super T5,F1<? super T6,F1<? super T7, ? extends R>>>>>>> curry() {
        return CurryVariance.curry7(this);
    }
   
}
