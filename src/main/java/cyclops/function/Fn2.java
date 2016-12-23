package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.ListX;
import cyclops.monads.WitnessType;
import cyclops.async.Future;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.function.Function2;

@FunctionalInterface
public interface Fn2<T1, T2, R> extends Function2<T1,T2,R> {

    public static <T1, T2, T3,R> Fn2<T1,T2, R> λ(final Fn2<T1,T2, R> triFunc){
        return triFunc;
    }
    public static <T1, T2, T3,R> Fn2<? super T1,? super T2,? extends R> λv(final Fn2<? super T1,? super T2,? extends R> triFunc){
        return triFunc;
    }
    
    public R apply(T1 a, T2 b);


    default <W extends WitnessType<W>> AnyMFn2<W,T1,T2,R> liftF(){
        return AnyM.liftF2(this);
    }
    
    
    default Fn2<T1, T2,  Maybe<R>> lift(){
        Fn2<T1, T2,  R> host = this;
       return (T1,T2)-> Maybe.fromLazy(Eval.later(()->Maybe.ofNullable(apply(T1,T2))));
    }
    default Fn2<T1, T2, Future<R>> lift(Executor ex){
        Fn2<T1, T2,  R> host = this;
       return (T1,T2)-> Future.ofSupplier(()->host.apply(T1,T2),ex);
    }
    default Fn2<T1, T2,  Try<R,Throwable>> liftTry(){
        Fn2<T1, T2,  R> host = this;
       return (T1,T2)->  Try.withCatch(()->host.apply(T1,T2),Throwable.class);
    }
    default Fn2<T1, T2,  Optional<R>> liftOpt(){
        Fn2<T1, T2,  R> host = this;
       return (T1,T2)-> Optional.ofNullable(host.apply(T1,T2));
    }

    
    default Fn2<T1,T2,R> memoize(){
        return Memoize.memoizeBiFunction(this);
    }
    default Fn2<T1,T2,R> memoize(Cacheable<R> c){
        return Memoize.memoizeBiFunction(this,c);
    }
    
    default Fn1<? super T1,Fn1<? super T2,? extends  R>> curry(){
        return CurryVariance.curry2(this);
    }
    
    
    default Fn1<T2, R> apply(final T1 s) {
        return Curry.curry2(this)
                    .apply(s);
    }

    default Fn2<T2,T1,R> reverse(){
        return (t2,t1)->apply(t1,t2);
    }
    @Override
    default <V> Fn2<T1, T2, V> andThen(Function<? super R, ? extends V> after) {
        return (t1,t2)-> after.apply(apply(t1,t2));
    }

    default FunctionalOperations<T1,T2,R> fnOps(){
        return (a,b)->apply(a,b);
    }

    interface FunctionalOperations<T1,T2,R> extends Fn2<T1,T2,R> {
        default <W extends WitnessType<W>> Fn2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> anyMZip() {
            return (a,b) -> (AnyM<W,R>)a.zip(b,this);
        }

        /**
        default <W extends WitnessType<W>> Fn2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> anyMM() {
            return (a,b) -> Matchables.fromEither5(a)
                                      .visit(v->v.forEach2(x->b,this),s->s.forEach2(x->b,this));
        }
         **/
        default Fn2<ReactiveSeq<T1>,ReactiveSeq<T2>, ReactiveSeq<R>> streamZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Fn2<ReactiveSeq<T1>,ReactiveSeq<T2>, ReactiveSeq<R>> streamM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Fn2<Future<T1>, Future<T2>, Future<R>> futureZip() {
            return (a,b) -> a.zip(b,this);
        }
        default Fn2<Future<T1>, Future<T2>, Future<R>> futureM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default <W extends WitnessType<W>> Fn2<FutureT<W,T1>,FutureT<W,T2>, FutureT<W,R>> futureTM(W witness) {
            return (a,b) -> a.forEach2M(x->b,this);
        }
        default <W extends WitnessType<W>> Fn2<FutureT<W,T1>,FutureT<W,T2>, FutureT<W,R>> futureTZip(W witness) {
            return (a,b) -> a.zip(b,this);
        }

        default Fn2<ListX<T1>,ListX<T2>, ListX<R>> listXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Fn2<ListX<T1>,ListX<T2>, ListX<R>> listXM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Fn2<PStackX<T1>,PStackX<T2>, PStackX<R>> pstackXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default Fn2<PStackX<T1>,PStackX<T2>, PStackX<R>> pstackXM() {
            return (a,b) -> a.forEach2(x->b,this);
        }
        default Fn2<PVectorX<T1>,PVectorX<T2>, PVectorX<R>> pvectorXZip() {
            return (a,b) -> a.zip(b,this);
        }

        default <W extends WitnessType<W>> Fn2<ListT<W,T1>,ListT<W,T2>, ListT<W,R>> listTM(W witness) {
            return (a,b) -> a.forEach2M(x->b,this);
        }
        default <W extends WitnessType<W>> Fn2<ListT<W,T1>,ListT<W,T2>, ListT<W,R>> listTZip(W witness) {
            return (a,b) -> a.zip(b,this);
        }
    }

}
