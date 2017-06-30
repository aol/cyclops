package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import cyclops.async.Future;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Try;

public interface Fn4<T1, T2, T3, T4, R> extends Fn1<T1,Fn1<T2,Fn1<T3,Fn1<T4,R>>>> {

    public R apply(T1 a, T2 b, T3 c, T4 d);
    
    default Fn4<T1, T2, T3, T4, Maybe<R>> lift4(){
       return (s1,s2,s3,s4)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3,s4))));
    }
    default Fn1<T2, Fn1<T3, Fn1<T4, R>>> apply(final T1 s) {
        return Curry.curry4(this)
                    .apply(s);
    }

    default Fn1<T3, Fn1<T4, R>> apply(final T1 s, final T2 s2) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2);
    }

    default Fn1<T4, R> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }
    default Fn4<T1,T2,T3,T4,R> memoize4(){
        return Memoize.memoizeQuadFunction(this);
    }
    default Fn4<T1,T2,T3,T4,R> memoize4(Cacheable<R> c){
        return Memoize.memoizeQuadFunction(this,c);
    }
    default Fn4<T1,T2,T3,T4, R> memoize4Async(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeQuadFunctionAsync(this,ex,cron);
    }
    default Fn4<T1,T2,T3,T4, R> memoize4Async(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeQuadFunctionAsync(this,ex,timeToLiveMillis);
    }
    default Fn4<T1, T2, T3, T4, Future<R>> lift4(Executor ex){
       
       return (s1,s2,s3,s4)-> Future.of(()->apply(s1,s2,s3,s4),ex);
    }
    default Fn4<T1, T2, T3, T4, Try<R,Throwable>> liftTry4(){
       return (s1,s2,s3,s4)->  Try.withCatch(()->apply(s1,s2,s3,s4),Throwable.class);
    }
    default Fn4<T1, T2, T3, T4, Optional<R>> liftOpt4(){
       
       return (s1,s2,s3,s4)-> Optional.ofNullable(apply(s1,s2,s3,s4));
    }
    
    default Fn1<? super T1,Fn1<? super T2,Fn1<? super T3,Fn1<? super T4,? extends  R>>>> curry(){
        return CurryVariance.curry4(this);
    }

    default <V> Fn4<T1, T2, T3, T4, V> andThen4(Function<? super R, ? extends V> after) {
        return (t1,t2,t3,t4)-> after.apply(apply(t1,t2,t3,t4));
    }

    public static <T1, T2, T3,T4,R> Fn4<T1,T2,T3,T4,R> λ(final Fn4<T1,T2,T3,T4,R> triFunc){
        return triFunc;
    }

    public static <T1, T2, T3,T4,R> Fn4<? super T1,? super T2,? super T3,? super T4,? extends R> λv(final Fn4<? super T1,? super T2,? super T3,? super T4,? extends R> triFunc){
        return triFunc;
    }
}
