package com.aol.cyclops.util.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.aol.cyclops.control.*;
import com.aol.cyclops.types.anyM.WitnessType;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;

@FunctionalInterface
public interface F2<T1, T2, R> extends Function2<T1,T2,R> {

    public static <T1, T2, T3,R> F2<T1,T2, R> λ(final F2<T1,T2, R> triFunc){
        return triFunc;
    }
    public static <T1, T2, T3,R> F2<? super T1,? super T2,? extends R> λv(final F2<? super T1,? super T2,? extends R> triFunc){
        return triFunc;
    }
    
    public R apply(T1 a, T2 b);


    default <W extends WitnessType<W>> MFunc2<W,T1,T2,R> liftF(){
        return AnyM.liftF2(this);
    }
    
    
    default F2<T1, T2,  Maybe<R>> lift(){
        F2<T1, T2,  R> host = this;
       return (T1,T2)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(T1,T2))));
    }
    default F2<T1, T2,  FutureW<R>> lift(Executor ex){
        F2<T1, T2,  R> host = this;
       return (T1,T2)-> FutureW.ofSupplier(()->host.apply(T1,T2),ex);
    }
    default F2<T1, T2,  Try<R,Throwable>> liftTry(){
        F2<T1, T2,  R> host = this;
       return (T1,T2)->  Try.withCatch(()->host.apply(T1,T2),Throwable.class);
    }
    default F2<T1, T2,  Optional<R>> liftOpt(){
        F2<T1, T2,  R> host = this;
       return (T1,T2)-> Optional.ofNullable(host.apply(T1,T2));
    }

    
    default F2<T1,T2,R> memoize(){
        return Memoize.memoizeBiFunction(this);
    }
    default F2<T1,T2,R> memoize(Cacheable<R> c){
        return Memoize.memoizeBiFunction(this,c);
    }
    
    default Function<? super T1,Function<? super T2,? extends  R>> curry(){
        return CurryVariance.curry2(this);
    }
    
    
    default Function<T2, R> apply(final T1 s) {
        return Curry.curry2(this)
                    .apply(s);
    }

    
}
