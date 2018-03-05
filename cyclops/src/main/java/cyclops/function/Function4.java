package cyclops.function;


import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import cyclops.control.*;

public interface Function4<T1, T2, T3, T4, R> extends Function1<T1,Function1<T2,Function1<T3,Function1<T4,R>>>> {

    public R apply(T1 a, T2 b, T3 c, T4 d);

    default Function4<T1, T2, T3, T4, Maybe<R>> lift4(){
       return (s1,s2,s3,s4)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4))));
    }
    default Function1<T2, Function1<T3, Function1<T4, R>>> apply(final T1 s) {
        return Curry.curry4(this)
                    .apply(s);
    }

    default Function1<T3, Function1<T4, R>> apply(final T1 s, final T2 s2) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2);
    }

    default Function1<T4, R> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }
    default Function4<T1,T2,T3,T4,R> memoize4(){
        return Memoize.memoizeQuadFunction(this);
    }
    default Function4<T1,T2,T3,T4,R> memoize4(Cacheable<R> c){
        return Memoize.memoizeQuadFunction(this,c);
    }
    default Function4<T1,T2,T3,T4, R> memoize4Async(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeQuadFunctionAsync(this,ex,cron);
    }
    default Function4<T1,T2,T3,T4, R> memoize4Async(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeQuadFunctionAsync(this,ex,timeToLiveMillis);
    }
    default Function4<T1, T2, T3, T4, Future<R>> lift4(Executor ex){

       return (s1,s2,s3,s4)-> Future.of(()->apply(s1,s2,s3,s4),ex);
    }
    default Function4<T1, T2, T3, T4, Try<R,Throwable>> liftTry4(){
       return (s1,s2,s3,s4)->  Try.withCatch(()->apply(s1,s2,s3,s4),Throwable.class);
    }
    default Function4<T1, T2, T3, T4, Option<R>> liftOpt4(){

       return (s1,s2,s3,s4)-> Option.ofNullable(apply(s1,s2,s3,s4));
    }

    default Function1<? super T1,Function1<? super T2,Function1<? super T3,Function1<? super T4,? extends  R>>>> curry(){
        return CurryVariance.curry4(this);
    }

    default <V> Function4<T1, T2, T3, T4, V> andThen4(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4)-> after.apply(apply(t1,t2,t3,t4));
    }

    public static <T1, T2, T3,T4,R> Function4<T1,T2,T3,T4,R> λ(final Function4<T1,T2,T3,T4,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,R> Function4<? super T1,? super T2,? super T3,? super T4,? extends R> λv(final Function4<? super T1,? super T2,? super T3,? super T4,? extends R> triFunc){
        return triFunc;
    }
}
