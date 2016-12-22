package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.jooq.lambda.function.Function4;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;

public interface F4<T1, T2, T3, T4, R> extends Function4<T1,T2,T3,T4,R> {

    public R apply(T1 a, T2 b, T3 c, T4 d);
    
    default F4<T1, T2, T3, T4, Maybe<R>> lift(){
       return (s1,s2,s3,s4)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4))));
    }
    default F1<T2, F1<T3, F1<T4, R>>> apply(final T1 s) {
        return Curry.curry4(this)
                    .apply(s);
    }

    default F1<T3, F1<T4, R>> apply(final T1 s, final T2 s2) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2);
    }

    default F1<T4, R> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }
    default F4<T1,T2,T3,T4,R> memoize(){
        return Memoize.memoizeQuadFunction(this);
    }
    default F4<T1,T2,T3,T4,R> memoize(Cacheable<R> c){
        return Memoize.memoizeQuadFunction(this,c);
    }
    default F4<T1, T2, T3, T4, FutureW<R>> lift(Executor ex){
       
       return (s1,s2,s3,s4)-> FutureW.ofSupplier(()->apply(s1,s2,s3,s4),ex);
    }
    default F4<T1, T2, T3, T4, Try<R,Throwable>> liftTry(){
       return (s1,s2,s3,s4)->  Try.withCatch(()->apply(s1,s2,s3,s4),Throwable.class);
    }
    default F4<T1, T2, T3, T4, Optional<R>> liftOpt(){
       
       return (s1,s2,s3,s4)-> Optional.ofNullable(apply(s1,s2,s3,s4));
    }
    
    default F1<? super T1,F1<? super T2,F1<? super T3,F1<? super T4,? extends  R>>>> curry(){
        return CurryVariance.curry4(this);
    }

    default <V> F4<T1, T2, T3, T4, V> andThen(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4)-> after.apply(apply(t1,t2,t3,t4));
    }

    public static <T1, T2, T3,T4,R> F4<T1,T2,T3,T4,R> λ(final F4<T1,T2,T3,T4,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,R> F4<? super T1,? super T2,? super T3,? super T4,? extends R> λv(final F4<? super T1,? super T2,? super T3,? super T4,? extends R> triFunc){
        return triFunc;
    }
}
