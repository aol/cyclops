package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import cyclops.control.*;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.SetX;
import cyclops.monads.WitnessType;
import cyclops.async.Future;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.jooq.lambda.function.Function1;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

@FunctionalInterface
public interface Fn1<T1,  R> extends Function1<T1,R> {
    static class µ {}
    public static <T1,  T3,R> Fn1<T1, R> λ(final Fn1<T1, R> triFunc){
        return triFunc;
    }
    public static <T1,  T3,R> Fn1<? super T1,? extends R> λv(final Fn1<? super T1,? extends R> triFunc){
        return triFunc;
    }

    default Reader<T1,R> reader(){
        return in->apply(in);
    }
    public R apply(T1 a);


    default Fn1<T1,Maybe<R>> lift(){
       return (T1)-> Maybe.fromLazy(Eval.later(()-> Maybe.ofNullable(apply(T1))));
    }
    default Fn1<T1, Future<R>> lift(Executor ex){
       return (T1)-> Future.ofSupplier(()->apply(T1),ex);
    }
    default Fn1<T1, Try<R,Throwable>> liftTry(){
       return (T1)->  Try.withCatch(()->apply(T1),Throwable.class);
    }
    default Fn1<T1,   Optional<R>> liftOpt(){
       return (T1)-> Optional.ofNullable(apply(T1));
    }

    default <W extends WitnessType<W>> AnyMFn1<W,T1,R> liftF(){
        return AnyM.liftF(this);
    }

    
    default Fn1<T1,R> memoize(){
        return Memoize.memoizeFunction(this);
    }
    default Fn1<T1,R> memoize(Cacheable<R> c){
        return Memoize.memoizeFunction(this,c);
    }

    default <R1> Fn1<T1,Tuple2<R,R1>> product(Fn1<? super T1, ? extends R1> fn){
        return in -> Tuple.tuple(apply(in),fn.apply(in));
    }


    default Fn0<R> bind(final T1 s) {
        return Curry.curry(this)
                    .apply(s);
    }

    @Override
    default <V> Fn1<V, R> compose(Function<? super V, ? extends T1> before) {
        return v -> apply(before.apply(v));
    }

    @Override
    default <V> Fn1<T1, V> andThen(Function<? super R, ? extends V> after) {
        return t -> after.apply(apply(t));
    }

    default <V> Fn1<Function<? super R, ? extends V>,Fn1<T1, V>> andThen() {
        return this::andThen;
    }



    default FunctionalOperations<T1,R> functionOps(){
        return in->apply(in);
    }

    interface FunctionalOperations<T1,R> extends Fn1<T1,R> {


        default <V> Fn1<T1, V> apply(final Function<? super T1,? extends Function<? super R,? extends V>> applicative) {
            return a -> applicative.apply(a).apply(this.apply(a));
        }

        default <R1> Fn1<T1, R1> map(final Function<? super R, ? extends R1> f2) {
            return andThen(f2);
        }

        default <R1> Fn1<T1, R1> flatMap(final Function<? super R, ? extends Function<? super T1, ? extends R1>> f) {
            return a -> f.apply(apply(a)).apply(a);
        }
        default <R1> Fn1<T1,R1> coflatMap(final Function<? super Fn1<? super T1,? extends R>, ? extends  R1> f) {
            return in-> f.apply(this);
        }

        default <W extends WitnessType<W>> AnyM<W, R> mapF(AnyM<W, T1> functor) {
            return functor.map(this);
        }
        default <W extends WitnessType<W>> FutureT<W,R> mapF(FutureT<W,T1> future) {
            return future.map(this);
        }
        default <W extends WitnessType<W>> ListT<W,R> mapF(ListT<W,T1> list) {
            return list.map(this);
        }
        default ListX<R> mapF(ListX<T1> list) {
            return list.map(this);
        }
        default DequeX<R> mapF(DequeX<T1> list) {
            return list.map(this);
        }
        default SetX<R> mapF(SetX<T1> set) {
            return set.map(this);
        }

        default PStackX<R> mapF(PStackX<T1> list) {
            return list.map(this);
        }

        default PVectorX<R> mapF(PVectorX<T1> list) {
            return list.map(this);
        }
        default Streamable<R> mapF(Streamable<T1> stream) {
            return stream.map(this);
        }

        default ReactiveSeq<R> mapF(ReactiveSeq<T1> stream) {
            return stream.map(this);
        }
        default Eval<R> mapF(Eval<T1> eval) {
            return eval.map(this);
        }
        default Maybe<R> mapF(Maybe<T1> maybe) {
            return maybe.map(this);
        }
        default <X extends Throwable> Try<R,X> mapF(Try<T1,X> xor) {
            return xor.map(this);
        }
        default <ST> Xor<ST,R> mapF(Xor<ST,T1> xor) {
            return xor.map(this);
        }
        default <ST> Ior<ST,R> mapF(Ior<ST,T1> ior) {
            return ior.map(this);
        }

        default Future<R> mapF(Future<T1> future) {
            return future.map(this);
        }

        default Fn1<T1, ReactiveSeq<R>> liftStream() {
            return in -> ReactiveSeq.of(apply(in));
        }

        default Fn1<T1, Future<R>> liftFuture() {
            return in -> Future.ofResult(apply(in));
        }
        default <W extends WitnessType<W>> Fn1<T1, FutureT<W,R>> liftFutureT(W witness) {
            return liftFuture().andThen(f->f.liftM(witness));
        }

        default Fn1<T1, ListX<R>> liftList() {
            return in -> ListX.of(apply(in));
        }
        default <W extends WitnessType<W>> Fn1<T1, ListT<W,R>> liftListT(W witness) {
            return liftList().andThen(l->l.liftM(witness));
        }

        default Fn1<T1, PStackX<R>> liftPStack() {
            return in -> PStackX.of(apply(in));
        }

        default Fn1<T1, PVectorX<R>> liftPVector() {
            return in -> PVectorX.of(apply(in));
        }
    }

}
