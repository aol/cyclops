package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import cyclops.async.Future;
import org.jooq.lambda.function.Function3;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Try;

@FunctionalInterface
public interface Fn3<S1, S2, S3, R> extends Function3<S1,S2,S3,R> {

    public static <T1, T2, T3,R> Fn3<T1,T2,T3, R> λ(final Fn3<T1,T2,T3, R> triFunc){
        return triFunc;
    }
    public static <T1, T2, T3,R> Fn3<? super T1,? super T2,? super T3,? extends R> λv(final Fn3<? super T1,? super T2,? super T3,? extends R> triFunc){
        return triFunc;
    }
    
    public R apply(S1 a, S2 b, S3 c);

    default Function3<S1, S2, S3, R> function3() {
        return this;
    }
    
    
    
    default Fn3<S1, S2, S3, Maybe<R>> lift(){
        Fn3<S1, S2, S3, R> host = this;
       return (s1,s2,s3)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(s1,s2,s3))));
    }
    default Fn3<S1, S2, S3, Future<R>> lift(Executor ex){
        Fn3<S1, S2, S3, R> host = this;
       return (s1,s2,s3)-> Future.ofSupplier(()->host.apply(s1,s2,s3),ex);
    }
    default Fn3<S1, S2, S3, Try<R,Throwable>> liftTry(){
        Fn3<S1, S2, S3, R> host = this;
       return (s1,s2,s3)->  Try.withCatch(()->host.apply(s1,s2,s3),Throwable.class);
    }
    default Fn3<S1, S2, S3, Optional<R>> liftOpt(){
        Fn3<S1, S2, S3, R> host = this;
       return (s1,s2,s3)-> Optional.ofNullable(host.apply(s1,s2,s3));
    }
    
    default Fn3<S1,S2,S3,R> memoize(){
        return Memoize.memoizeTriFunction(this);
    }
    default Fn3<S1,S2,S3,R> memoize(Cacheable<R> c){
        return Memoize.memoizeTriFunction(this,c);
    }
    
    default Fn1<? super S1,Fn1<? super S2,Fn1<? super S3,? extends  R>>> curry(){
        return CurryVariance.curry3(this);
    }
    
    
    default Fn1<S2, Fn1<S3, R>> apply(final S1 s) {
        return Curry.curry3(this)
                    .apply(s);
    }

    default Fn1<S3, R> apply(final S1 s, final S2 s2) {
        return Curry.curry3(this)
                    .apply(s)
                    .apply(s2);
    }


    default <V> Fn3<S1, S2, S3, V> andThen(Function<? super R, ? extends V> after) {
        return (s1,s2,s3)-> after.apply(apply(s1,s2,s3));
    }
}
