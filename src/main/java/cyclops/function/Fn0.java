package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import cyclops.monads.WitnessType;
import org.jooq.lambda.function.Function0;

import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.control.Maybe;
import cyclops.control.Try;

@FunctionalInterface
public interface Fn0< R> extends Function0<R>{

    public static <  T3,R> Fn0< R> λ(final Fn0<R> triFunc){
        return triFunc;
    }
    public static <  T3,R> Fn0<? extends R> λv(final Fn0<? extends R> triFunc){
        return triFunc;
    }

    default Fn0<Maybe<R>> lift(){
        return ()-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply())));
    }

    default Fn0<Future<R>> lift(Executor ex){
       return ()-> Future.ofSupplier(()->apply(),ex);
    }
    default Fn0<   Try<R,Throwable>> liftTry(){
       return ()->  Try.withCatch(()->apply(),Throwable.class);
    }
    default Fn0<   Optional<R>> liftOpt(){
       return ()-> Optional.ofNullable(apply());
    }

    default <W extends WitnessType<W>> AnyMFn0<W,R> liftF(W witness){
        return ()-> witness.adapter().unit(this.get());
    }
    
    default Fn0<R> memoize(){
        return Memoize.memoizeSupplier(this);
    }
    default Fn0<R> memoize(Cacheable<R> c){
        return Memoize.memoizeSupplier(this,c);
    }



    default <V> Fn0<V> andThen(Function<? super R, ? extends V> after) {
        return () -> after.apply(get());
    }

    
}
