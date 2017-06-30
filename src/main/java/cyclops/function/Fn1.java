package cyclops.function;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import cyclops.collections.immutable.VectorX;
import cyclops.companion.CompletableFutures;
import cyclops.control.*;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.monads.Witness;
import cyclops.monads.Witness.reader;
import cyclops.monads.function.AnyMFn1;

import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.DequeX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.monads.WitnessType;
import cyclops.async.Future;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.Traverse;
import org.jooq.lambda.function.Function1;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

@FunctionalInterface
public interface Fn1<T1,  R> extends Function1<T1,R> {

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

    /**
     * Apply before advice toNested this function, capture the input with the provided Consumer
     *
     * @param action Before advice
     * @return Function with Before advice attached
     */
    default Fn1<T1, R> before(final Consumer<? super T1> action){
        return FluentFunctions.of(this).before(action);
    }
    /**
     * Apply After advice toNested this function capturing both the input and the emitted with the provided BiConsumer
     *
     * @param action After advice
     * @return  Function with After advice attached
     */
    default Fn1<T1, R> after(final BiConsumer<? super T1,? super R> action) {
        return FluentFunctions.of(this).after(action);
    }

    default Fn1<T1,Maybe<R>> lift(){
       return (T1)-> Maybe.fromLazy(Eval.later(()-> Maybe.ofNullable(apply(T1))));
    }
    default Fn1<T1, Future<R>> lift(Executor ex){
       return (T1)-> Future.of(()->apply(T1),ex);
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
    default Fn1<T1, R> memoizeAsync(ScheduledExecutorService ex, String cron){
        return Memoize.memoizeFunctionAsync(this,ex,cron);
    }
    default Fn1<T1, R> memoizeAsync(ScheduledExecutorService ex, long timeToLiveMillis){
        return Memoize.memoizeFunctionAsync(this,ex,timeToLiveMillis);
    }

    default <T2,R2> Fn1<Xor<T1, T2>, Xor<R, R2>> merge(Function<? super T2, ? extends R2> fn) {
        Fn1<T1, Xor<R, R2>> first = andThen(Xor::secondary);
        Function<? super T2, ? extends Xor<R,R2>> second = fn.andThen(Xor::primary);
        return first.fanIn(second);

    }

    default <T2> Fn1<Xor<T1, T2>, R> fanIn(Function<? super T2, ? extends R> fanIn) {
        return e ->   e.visit(this, fanIn);
    }
    default <__> Fn1<Xor<T1, __>, Xor<R, __>> leftFn() {

        return either->  either.bimap(this,Function.identity());
    }
    default <__> Fn1<Xor<__,T1>, Xor<__,R>> rightFn() {

        return either->  either.bimap(Function.identity(),this);
    }


    default <R1> Fn1<T1,Tuple2<R,R1>> product(Fn1<? super T1, ? extends R1> fn){
        return in -> Tuple.tuple(apply(in),fn.apply(in));
    }

    default <__> Fn1<Tuple2<T1, __>, Tuple2<R, __>> firstFn() {

        return t-> Tuple.tuple(apply(t.v1),t.v2);
    }
    default <__> Fn1<Tuple2<__, T1>, Tuple2<__, R>> secondFn() {

        return t-> Tuple.tuple(t.v1,apply(t.v2));
    }




    default <R2,R3> Fn1<T1, Tuple3<R, R2, R3>> product(Function<? super T1, ? extends R2> fn2, Function<? super T1, ? extends R3> fn3) {
        return a -> Tuple.tuple(apply(a), fn2.apply(a),fn3.apply(a));
    }
    default <R2,R3,R4> Fn1<T1, Tuple4<R, R2,R3,R4>> product(Function<? super T1, ? extends R2> fn2,
                                                           Function<? super T1, ? extends R3> fn3,
                                                           Function<? super T1, ? extends R4> fn4) {
        return a -> Tuple.tuple(apply(a), fn2.apply(a),fn3.apply(a),fn4.apply(a));
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



    static <T,R> Fn1<T,R> narrow(Function<? super T, ? extends R> fn){
        if(fn instanceof Fn1){
            return (Fn1<T,R>)fn;
        }
        return t->fn.apply(t);
    }
    default FunctionalOperations<T1,R> functionOps(){
        return in->apply(in);
    }

    interface FunctionalOperations<T1,R> extends Fn1<T1,R>{


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

        default LinkedListX<R> mapF(LinkedListX<T1> list) {
            return list.map(this);
        }

        default VectorX<R> mapF(VectorX<T1> list) {
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

        default Fn1<T1, LinkedListX<R>> liftPStack() {
            return in -> LinkedListX.of(apply(in));
        }

        default Fn1<T1, VectorX<R>> liftPVector() {
            return in -> VectorX.of(apply(in));
        }
    }


}
