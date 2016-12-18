package com.aol.cyclops.util.function;

import java.util.Optional;
import java.util.concurrent.Executor;

import com.aol.cyclops.types.anyM.WitnessType;
import org.jooq.lambda.function.Function0;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.types.Value;

@FunctionalInterface
public interface F0< R> extends Function0<R>, Value<R> {

    public static <  T3,R> F0< R> λ(final F0<R> triFunc){
        return triFunc;
    }
    public static <  T3,R> F0<? extends R> λv(final F0<? extends R> triFunc){
        return triFunc;
    }
    
    default F0<Maybe<R>> lift(){
       return ()-> Maybe.fromEval(Eval.later(()->apply()));
    }
    default F0<   FutureW<R>> lift(Executor ex){
       return ()-> FutureW.ofSupplier(()->apply(),ex);
    }
    default F0<   Try<R,Throwable>> liftTry(){
       return ()->  Try.withCatch(()->apply(),Throwable.class);
    }
    default F0<   Optional<R>> liftOpt(){
       return ()-> Optional.ofNullable(apply());
    }

    default <W extends WitnessType<W>>  MFunc0<W,R> liftF(W witness){
        return ()-> witness.adapter().unit(this.get());
    }
    
    default F0<R> memoize(){
        return Memoize.memoizeSupplier(this);
    }
    default F0<R> memoize(Cacheable<R> c){
        return Memoize.memoizeSupplier(this,c);
    }
    
    
    

    
}
